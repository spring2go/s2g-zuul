package io.spring2go.zuul.filters;

import io.spring2go.zuul.common.Constants;
import io.spring2go.zuul.common.FilterInfo;
import io.spring2go.zuul.common.IZuulFilterDao;
import io.spring2go.zuul.util.JSONUtils;
import com.netflix.config.DynamicBooleanProperty;
import com.netflix.config.DynamicPropertyFactory;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;

/**
 * Servlet for uploading/downloading/managing scripts.
 * <p/>
 * <ul>
 * <li>Upload scripts to the registry for a given endpoint.</li>
 * <li>Download scripts from the registry</li>
 * <li>List all revisions of scripts for a given endpoint.</li>
 * <li>Mark a particular script revision as active for production.</li>
 * </ul>
 */
@ThreadSafe
public class FilterScriptManagerServlet extends HttpServlet {

    public static final DynamicBooleanProperty adminEnabled =
            DynamicPropertyFactory.getInstance().getBooleanProperty(Constants.ZUUL_FILTER_ADMIN_ENABLED, true);

    private static final long serialVersionUID = -1L;
    private static final Logger logger = LoggerFactory.getLogger(FilterScriptManagerServlet.class);

    /* actions that we permit as an immutable Set */
    private static final Set<String> VALID_GET_ACTIONS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(new String[]{"LIST", "DOWNLOAD"})));
    private static final Set<String> VALID_PUT_ACTIONS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(new String[]{"UPLOAD", "ACTIVATE", "DEACTIVATE", "RUN", "CANARY"})));

    /* DAO for performing CRUD operations with scripts */
    private IZuulFilterDao scriptDAO;

    /* Controller for executing scripts in development/test */


    /**
     * Default constructor that instantiates default dependencies (ie. the ones that are functional as opposed to those for testing).
     */
    public FilterScriptManagerServlet() {
        this(ZuulFilterDaoFactory.getZuulFilterDao());

    }

    /**
     * Construct with dependency injection for unit-testing (will never be invoked in production since servlets can't have constructors)
     *
     * @param scriptDAO
     */
    private FilterScriptManagerServlet(IZuulFilterDao scriptDAO) {
        super();
        this.scriptDAO = scriptDAO;
    }

    /**
     * GET a script or list of scripts.
     * <p/>
     * Action: LIST
     * <p/>
     * Description: List of all script revisions for the given endpoint URI or list all endpoints if endpoint URI not given.
     * <ul>
     * <li>Request Parameter "endpoint": URI</li>
     * </ul>
     * <p/>
     * Action: DOWNLOAD
     * <p/>
     * Description: Download the text or zip file of scripts for a given endpoint URI + revision.
     * <ul>
     * <li>Request Parameter "endpoint": URI</li>
     * <li>Request Parameter "revision": int of revision to download</li>
     * </ul>
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // retrieve arguments and validate
        String action = request.getParameter("action");
        /* validate the action and method */
        if (!isValidAction(request, response)) {
            return;
        }

        try {
            // perform action
            if ("LIST".equals(action)) {
                handleListAction(request, response);
            } else if ("DOWNLOAD".equals(action)) {
                handleDownloadAction(request, response);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    /**
     * PUT a script
     * <p/>
     * Action: UPLOAD
     * <p/>
     * Description: Upload a new script text or zip file for a given endpoint URI.
     * <ul>
     * <li>Request Parameter "endpoint": URI</li>
     * <li>Request Parameter "userAuthenticationRequired": true/false</li>
     * <li>POST Body: text or zip file with multiple text files</li>
     * </ul>
     * <p/>
     * Action: ACTIVATE
     * <p/>
     * Description: Activate a script to become the default to execute for a given endpoint URI + revision.
     * <ul>
     * <li>Request Parameter "endpoint": URI</li>
     * <li>Request Parameter "revision": int of revision to activate</li>
     * </ul>
     */
    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if (! adminEnabled.get()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Filter admin is disabled. See the zuul.filters.admin.enabled FastProperty.");
            return;
        }

        // retrieve arguments and validate
        String action = request.getParameter("action");
        /* validate the action and method */
        if (!isValidAction(request, response)) {
            return;
        }

        try {
            // perform action
            if ("UPLOAD".equals(action)) {
                handleUploadAction(request, response);
            } else if ("ACTIVATE".equals(action)) {
                handleActivateAction(request, response);
            } else if ("CANARY".equals(action)) {
                handleCanaryAction(request, response);
            } else if ("DEACTIVATE".equals(action)) {
                handledeActivateAction(request, response);
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPut(request, response);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        setUsageError(405, response);
        return;
    }

    private void handleListAction(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String filter_id = request.getParameter("filter_id");
        if (filter_id == null) {
            // get list of all endpoints
            List<String> filterIDs = scriptDAO.getAllFilterIds();
            Map<String, Object> json = new LinkedHashMap<String, Object>();
            json.put("filters", filterIDs);
            response.getWriter().write(JSONUtils.jsonFromMap(json));
        } else {
            List<FilterInfo> scripts;
            if (Boolean.parseBoolean(request.getParameter("active"))) {
                // get list of all scripts for this endpoint
                FilterInfo activeEndpoint = scriptDAO.getActiveFilter(filter_id);
                scripts = activeEndpoint == null ? Collections.EMPTY_LIST : Collections.singletonList(activeEndpoint);
            } else {
                // get list of all scripts for this endpoint
                scripts = scriptDAO.getZuulFilters(filter_id);
            }
            if (scripts.size() == 0) {
                setUsageError(404, "ERROR: No scripts found for endpoint: " + filter_id, response);
            } else {
                // output JSON
                Map<String, Object> json = new LinkedHashMap<String, Object>();
                json.put("filter_id", filter_id);
                List<Map<String, Object>> scriptsJson = new ArrayList<Map<String, Object>>();
                for (FilterInfo script : scripts) {
                    Map<String, Object> scriptJson = createEndpointScriptJSON(script);
                    scriptsJson.add(scriptJson);
                }

                json.put("filters", scriptsJson);

                response.getWriter().write(JSONUtils.jsonFromMap(json));
            }
        }
    }

    private void handleDownloadAction(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String filter_id = request.getParameter("filter_id");
        if (filter_id == null) {
            // return error, endpoint is required
            setUsageError(404, "ERROR: No endpoint defined.", response);
        } else {
            String revision = request.getParameter("revision");
            FilterInfo script = null;
            if (revision == null) {
                // get latest
                script = scriptDAO.getLatestFilter(filter_id);
            } else {
                int revisionNumber = -1;
                try {
                    revisionNumber = Integer.parseInt(revision);
                } catch (Exception e) {
                    setUsageError(400, "ERROR: revision must be an integer.", response);
                    return;
                }
                // get the specific revision
                script = scriptDAO.getFilter(filter_id, revisionNumber);
            }

            // now output script
            if (script == null) {
                setUsageError(404, "ERROR: No scripts found.", response);
            } else {
                if (script.getFilterCode() == null) {
                    // this shouldn't occur but I want to handle it if it does
                    logger.error("Found FilterInfo object without scripts. Length==0. Request: " + request.getPathInfo());
                    setUsageError(500, "ERROR: script files not found", response);
                } else {
                    // output the single script
                    response.setContentType("text/plain");
                    response.getWriter().write(script.getFilterCode());
                }
            }
        }
    }

    private void handleCanaryAction(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String filter_id = request.getParameter("filter_id");
        if (filter_id == null) {
            // return error, endpoint is required
            setUsageError(404, "ERROR: No endpoint defined.", response);
        } else {
            String revision = request.getParameter("revision");
            if (revision == null) {
                setUsageError(404, "ERROR: No revision defined.", response);
            } else {
                int revisionNumber = -1;
                try {
                    revisionNumber = Integer.parseInt(revision);
                } catch (Exception e) {
                    setUsageError(400, "ERROR: revision must be an integer.", response);
                    return;
                }
                FilterInfo filterInfo = scriptDAO.canaryFilter(filter_id, revisionNumber);
//                Map<String, Object> scriptJson = createEndpointScriptJSON(filterInfo);
//                response.getWriter().write(JsonUtility.jsonFromMap(scriptJson));
                response.sendRedirect("filterLoader.jsp");

            }
        }

    }


    private void handledeActivateAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String filter_id = request.getParameter("filter_id");
        if (filter_id == null) {
            // return error, endpoint is required
            setUsageError(404, "ERROR: No endpoint defined.", response);
        } else {
            String revision = request.getParameter("revision");
            if (revision == null) {
                setUsageError(404, "ERROR: No revision defined.", response);
            } else {
                int revisionNumber = -1;
                try {
                    revisionNumber = Integer.parseInt(revision);
                } catch (Exception e) {
                    setUsageError(400, "ERROR: revision must be an integer.", response);
                    return;
                }
                try {
                    FilterInfo filterInfo = scriptDAO.deactivateFilter(filter_id, revisionNumber);
                } catch (Exception e) {
                    setUsageError(400, "ERROR: " + e.getMessage(), response);
                    return;
                }
                response.sendRedirect("filterLoader.jsp");

//                Map<String, Object> scriptJson = createEndpointScriptJSON(filterInfo);
                //              response.getWriter().write(JsonUtility.jsonFromMap(scriptJson));
            }
        }

    }

    private void handleActivateAction(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String filter_id = request.getParameter("filter_id");
        if (filter_id == null) {
            // return error, endpoint is required
            setUsageError(404, "ERROR: No endpoint defined.", response);
        } else {
            String revision = request.getParameter("revision");
            if (revision == null) {
                setUsageError(404, "ERROR: No revision defined.", response);
            } else {
                int revisionNumber = -1;
                try {
                    revisionNumber = Integer.parseInt(revision);
                } catch (Exception e) {
                    setUsageError(400, "ERROR: revision must be an integer.", response);
                    return;
                }
                try {
                    FilterInfo filterInfo = scriptDAO.activateFilter(filter_id, revisionNumber);
                } catch (Exception e) {
                    setUsageError(400, "ERROR: " + e.getMessage(), response);
                    return;
                }
                response.sendRedirect("filterLoader.jsp");

//                Map<String, Object> scriptJson = createEndpointScriptJSON(filterInfo);
                //              response.getWriter().write(JsonUtility.jsonFromMap(scriptJson));
            }
        }

    }

    private void handleUploadAction(HttpServletRequest request, HttpServletResponse response) throws Exception {

        String filter = handlePostBody(request, response);


        if (filter != null) {
            FilterInfo filterInfo = null;
            try {
                filterInfo = FilterVerifier.getInstance().verifyFilter(filter);
            } catch (IllegalAccessException e) {
                logger.error(e.getMessage(), e);
                setUsageError(500, "ERROR: Unable to process uploaded data. " + e.getMessage(), response);
            } catch (InstantiationException e) {
                logger.error(e.getMessage(), e);
                setUsageError(500, "ERROR: Bad Filter. " + e.getMessage(), response);
            }
            filterInfo = scriptDAO.addFilter(filter, filterInfo.getFilterType(), filterInfo.getFilterName(), filterInfo.getFilterDisablePropertyName(), filterInfo.getFilterOrder());
            if (filterInfo == null) {
                setUsageError(500, "ERROR: Unable to process uploaded data.", response);
                return;
            }
            response.sendRedirect("filterLoader.jsp");

//            Map<String, Object> scriptJson = createEndpointScriptJSON(filterInfo);
//            response.getWriter().write(JsonUtility.jsonFromMap(scriptJson));
        }
    }


    private String handlePostBody(HttpServletRequest request, HttpServletResponse response) throws IOException {

        FileItemFactory factory = new DiskFileItemFactory();
        ServletFileUpload upload = new ServletFileUpload(factory);
        org.apache.commons.fileupload.FileItemIterator it = null;
        try {
            it = upload.getItemIterator(request);

            while (it.hasNext()) {
                FileItemStream stream = it.next();
                InputStream input = stream.openStream();

                // NOTE: we are going to pull the entire stream into memory
                // this will NOT work if we have huge scripts, but we expect these to be measured in KBs, not MBs or larger
                byte[] uploadedBytes = getBytesFromInputStream(input);
                input.close();

                if (uploadedBytes.length == 0) {
                    setUsageError(400, "ERROR: Body contained no data.", response);
                    return null;
                }

                return new String(uploadedBytes);
            }
        } catch (FileUploadException e) {
            throw new IOException(e.getMessage());
        }
        return null;
    }

    private byte[] getBytesFromInputStream(InputStream input) throws IOException {
        int v = 0;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        while ((v = input.read()) != -1) {
            bos.write(v);
        }
        bos.close();
        return bos.toByteArray();
    }

    private Map<String, Object> createEndpointScriptJSON(FilterInfo script) {
        Map<String, Object> scriptJson = new LinkedHashMap<String, Object>();
        scriptJson.put("filter_id", script.getFilterId());
        scriptJson.put("filter_name", script.getFilterName());
        scriptJson.put("filter_type", script.getFilterType());
        scriptJson.put("revision", script.getRevision());
        scriptJson.put("active", script.isActive());
        scriptJson.put("creationDate", script.getCreateTime());
        scriptJson.put("canary", script.isCanary());
        return scriptJson;
    }

    /**
     * Determine if the incoming action + method is a correct combination. If not, output the usage docs and set an error code on the response.
     *
     * @param request
     * @param response
     * @return true if valid, false if not
     */
    private static boolean isValidAction(HttpServletRequest request, HttpServletResponse response) {
        String action = request.getParameter("action");
        if (action != null) {
            action = action.trim().toUpperCase();
            /* test for GET actions */
            if (VALID_GET_ACTIONS.contains(action)) {
                if (!request.getMethod().equals("GET")) {
                    // valid action, wrong method
                    setUsageError(405, "ERROR: Invalid HTTP method for action type.", response);
                    return false;
                }
                // valid action and method
                return true;
            }

            if (VALID_PUT_ACTIONS.contains(action)) {
                if (!(request.getMethod().equals("PUT") || request.getMethod().equals("POST"))) {
                    // valid action, wrong method
                    setUsageError(405, "ERROR: Invalid HTTP method for action type.", response);
                    return false;
                }
                // valid action and method
                return true;
            }

            // wrong action
            setUsageError(400, "ERROR: Unknown action type.", response);
            return false;
        } else {
            setUsageError(400, "ERROR: Invalid arguments.", response);
            return false;
        }
    }

    /**
     * Set an error code and print out the usage docs to the response with a preceding error message
     *
     * @param statusCode
     * @param response
     */
    private static void setUsageError(int statusCode, String message, HttpServletResponse response) {
        response.setStatus(statusCode);
        try {
            Writer w = response.getWriter();
            if (message != null) {
                w.write(message + "\n\n");
            }
            w.write(getUsageDoc());
        } catch (Exception e) {
            logger.error("Failed to output usage error.", e);
            // won't throw exception because this is not critical, logging the error is enough
        }
    }

    /**
     * Set an error code and print out the usage docs to the response.
     *
     * @param statusCode
     * @param response
     */
    private static void setUsageError(int statusCode, HttpServletResponse response) {
        setUsageError(statusCode, null, response);
    }

    /**
     * Usage documentation to be output when a URL is malformed.
     *
     * @return
     */
    private static String getUsageDoc() {
        StringBuilder s = new StringBuilder();
        s.append("Usage: /scriptManager?action=<ACTION_TYPE>&<ARGS>").append("\n");
        s.append("       Actions:").append("\n");
        s.append("          LIST: List all endpoints with scripts or all scripts for a given endpoint.").append("\n");
        s.append("              Arguments:").append("\n");
        s.append("                  endpoint: [Optional (Default: All endpoints)] The endpoint of script revisions to list.").append("\n");
        s.append("              Examples:").append("\n");
        s.append("                GET /scriptManager?action=LIST").append("\n");
        s.append("                GET /scriptManager?action=LIST&endpoint=/ps3/home").append("\n");
        s.append("\n");

        s.append("          DOWNLOAD: Download a given script.").append("\n");
        s.append("              Arguments:").append("\n");
        s.append("                  endpoint: [Required] The endpoint of script to download.").append("\n");
        s.append("                  revision: [Optional (Default: last revision)] The revision to download.").append("\n");
        s.append("              Examples:").append("\n");
        s.append("                GET /scriptManager?action=DOWNLOAD&endpoint=/ps3/home").append("\n");
        s.append("                GET /scriptManager?action=DOWNLOAD&endpoint=/ps3/home&revision=23").append("\n");
        s.append("\n");

        s.append("          UPLOAD: Upload a script for a given endpoint.").append("\n");
        s.append("              Arguments:").append("\n");
        s.append("                  endpoint: [Required] The endpoint to associated the script with. If it doesn't exist it will be created.").append("\n");
        s.append("                  userAuthenticationRequired: [Optional (Default: true)] Whether the script requires an authenticated user to execute.").append("\n");
        s.append("              Example:").append("\n");
        s.append("                POST /scriptManager?action=UPLOAD&endpoint=/ps3/home").append("\n");
        s.append("                POST /scriptManager?action=UPLOAD&endpoint=/ps3/home&userAuthenticationRequired=false").append("\n");
        s.append("\n");

        s.append("          ACTIVATE: Mark a particular script revision as active for production.").append("\n");
        s.append("              Arguments:").append("\n");
        s.append("                  endpoint: [Required] The endpoint for which a script revision should be activated.").append("\n");
        s.append("                  revision: [Required] The script revision to activate.").append("\n");
        s.append("              Example:").append("\n");
        s.append("                PUT /scriptManager?action=ACTIVATE&endpoint=/ps3/home&revision=22").append("\n");
        return s.toString();
    }
}
