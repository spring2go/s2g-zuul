package io.spring2go.zuul.filters;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.spring2go.zuul.common.FilterInfo;
import io.spring2go.zuul.common.IZuulFilterDao;
import com.zaxxer.hikari.HikariDataSource;

public class JDBCZuulFilterDao implements IZuulFilterDao {
	private static final Logger LOGGER = LoggerFactory.getLogger(JDBCZuulFilterDao.class);
	private String filterTable;
	private HikariDataSource dataSource;
	private String applicationName;
	private String SQL;

	public JDBCZuulFilterDao(String filterTable, HikariDataSource dataSource, String applicationName) {
		this.filterTable = filterTable;
		this.dataSource = dataSource;
		this.applicationName = applicationName;

		this.SQL = "select filter_id,revision,create_time,is_active,is_canary,filter_code,filter_type,filter_name,disable_property_name,filter_order,application_name from "
				+ filterTable;
	}

	@Override
	public List<String> getAllFilterIds() throws Exception {

		Connection connection = dataSource.getConnection();

		Statement s = null;
		ResultSet r = null;

		List<String> list = new ArrayList<String>();

		try {
			connection.setAutoCommit(true);
			s = connection.createStatement();

			r = s.executeQuery("select distinct filter_id from " + filterTable);

			while (r.next()) {
				list.add(r.getString(1));
			}
		} finally {
			try {
				if (r != null) {
					r.close();
				}
				if (s != null) {
					s.close();
				}
			} finally {
				connection.close();
			}
		}
		return list;
	}

	@Override
	public List<FilterInfo> getZuulFilters(String filterId) throws Exception {

		Connection connection = dataSource.getConnection();
		PreparedStatement ps = null;
		ResultSet r = null;
		List<FilterInfo> list = new ArrayList<FilterInfo>();

		try {
			connection.setAutoCommit(true);
			ps = connection.prepareStatement(SQL + " where filter_id = ?");
			ps.setString(1, filterId);
			r = ps.executeQuery();

			while (r.next()) {
				list.add(buildFilterInfo(r));
			}
		} finally {
			try {
				if (r != null) {
					r.close();
				}

				if (ps != null) {
					ps.close();
				}
			} finally {
				connection.close();
			}
		}

		return list;
	}

	@Override
	public FilterInfo getFilter(String filterId, int revision) throws Exception {
		Connection connection = dataSource.getConnection();
		PreparedStatement ps = null;
		ResultSet r = null;
		FilterInfo filterInfo = null;

		try {
			connection.setAutoCommit(true);
			ps = connection.prepareStatement(SQL + " where filter_id = ? and revision = ?");
			ps.setString(1, filterId);
			ps.setInt(2, revision);

			r = ps.executeQuery();

			while (r.next()) {
				filterInfo = buildFilterInfo(r);
			}
		} finally {
			try {
				if (r != null) {
					r.close();
				}
				if (ps != null) {
					ps.close();
				}
			} finally {
				connection.close();
			}
		}
		return filterInfo;
	}

	@Override
	public FilterInfo getLatestFilter(String filterId) throws Exception {
		Connection connection = dataSource.getConnection();
		PreparedStatement ps = null;
		ResultSet r = null;
		FilterInfo filterInfo = null;
		
		try{
			connection.setAutoCommit(true);
			ps = connection.prepareStatement(SQL + " where filter_id = ? order by revision desc");
			
			ps.setString(1, filterId);
			r = ps.executeQuery();
			if(r.next()){
				filterInfo = buildFilterInfo(r);
			}
		}finally{
			try{
				if(r != null){
					r.close();
				}
				if(ps != null){
					ps.close();
				}
			}finally{
				connection.close();
			}
		}

		return filterInfo;
	}

	@Override
	public FilterInfo getActiveFilter(String filterId) throws Exception {

		Connection connection = dataSource.getConnection();
		PreparedStatement ps = null;
		ResultSet r = null;
		FilterInfo filterInfo = null;
		
		try{
			connection.setAutoCommit(true);
			ps = connection.prepareStatement(SQL + " where filter_id = ? and is_active = ?");
			ps.setString(1, filterId);
			ps.setBoolean(2, true);
			
			r = ps.executeQuery();
			if(r.next()){
				filterInfo = buildFilterInfo(r);
			}
		}finally{
			try{
				if(r != null){
					r.close();
				}
				if(ps != null){
					ps.close();
				}
			}finally{
				connection.close();
			}
		}
		return filterInfo;
	}

	@Override
	public List<FilterInfo> getAllCanaryFilters() throws Exception {
		
		Connection connection = dataSource.getConnection();
		PreparedStatement ps = null;
		ResultSet r = null;
		List<FilterInfo> list = new ArrayList<FilterInfo>(); 

		try{
			
			connection.setAutoCommit(true);
			ps = connection.prepareStatement(SQL + " where is_canary = ?");
			ps.setBoolean(1, true);
			
			r = ps.executeQuery();
			while(r.next()){
				list.add(buildFilterInfo(r));
			}
			
		}finally{
			try{
				if(r != null){
					r.close();
				}
				if(ps != null){
					ps.close();
				}
			}finally{
				connection.close();
			}
		}
		return list;
	}

	@Override
	public List<FilterInfo> getAllActiveFilters() throws Exception {

		Connection connection = dataSource.getConnection();
		PreparedStatement ps = null;
		ResultSet r = null;
		
		List<FilterInfo> list = new ArrayList<FilterInfo>(); 
		
		try{
			
			connection.setAutoCommit(true);
			ps = connection.prepareStatement(SQL + " where is_active = ?");
			ps.setBoolean(1, true);
			
			r = ps.executeQuery();
			while(r.next()){
				list.add(buildFilterInfo(r));
			}

		}finally{
			try{
				if( r != null){
					r.close();
				}
				
				if(ps != null){
					ps.close();
				}
			}finally{
				connection.close();
			}
		}
		return list;
	}

	@Override
	public FilterInfo canaryFilter(String filterId, int revision) throws Exception {

		Connection connection = dataSource.getConnection();
		PreparedStatement ps1 = null,ps2=null;
		
		try{
			
			connection.setAutoCommit(false);
			
			ps1 = connection.prepareStatement("update " + filterTable +" set is_active=?,is_canary=? where filter_id=? and revision=? ");
			ps1.setBoolean(1, false);
			ps1.setBoolean(2, true);
			ps1.setString(3, filterId);
			ps1.setInt(4, revision);
			
			int rowCount = ps1.executeUpdate();
			
			if(rowCount < 1){
				throw new Exception("Filter not Found " + filterId + " revision:" + revision);
			}
			
			ps2 = connection.prepareStatement("update " + filterTable + " set is_active=?,is_canary=? where is_canary=? and filter_id=? and revision !=?");
			ps2.setBoolean(1, false);
			ps2.setBoolean(2, false);
			ps2.setBoolean(3, true);
			ps2.setString(4, filterId);
			ps2.setInt(5, revision);
			
			ps2.executeUpdate();
			
			connection.commit();			
		}finally{
			try{
				if(ps2 != null){
					ps2.close();
				}
				
				if(ps1 != null){
					ps1.close();
				}
			}finally{
				connection.close();
			}
		}

		return getFilter(filterId, revision);
	}

	@Override
	public FilterInfo activateFilter(String filterId, int revision) throws Exception {
		Connection connection = dataSource.getConnection();
		PreparedStatement ps1 = null,ps2 = null;
		
		try{
			connection.setAutoCommit(false);
			ps1 = connection.prepareStatement("update " + filterTable +" set is_active=?,is_canary=? where is_canary=? and filter_id=? and revision=?");
			ps1.setBoolean(1, true);
			ps1.setBoolean(2, false);
			ps1.setBoolean(3, true);
			ps1.setString(4, filterId);
			ps1.setInt(5, revision);
			
			int rowCount = ps1.executeUpdate();
			
            if (rowCount < 1) {
                throw new RuntimeException("Filter not Found " + filterId + "revision:" + revision + ". Or " +
                "Filter must be canaried before activated ");
            }
            
            ps2 = connection.prepareStatement("update " + filterTable +" set is_active=?,is_canary=? where is_active=? and filter_id=? and revision != ?");
            ps2.setBoolean(1, false);
            ps2.setBoolean(2, false);
            ps2.setBoolean(3, true);
            ps2.setString(4, filterId);
            ps2.setInt(5, revision);
            ps2.executeUpdate();
            
            connection.commit();
		}finally{
			try{
				if(ps2 != null){
					ps2.close();
				}
				
				if(ps1 != null){
					ps1.close();
				}
			}finally{
				connection.close();
			}
		}
        return getFilter(filterId, revision);
	}

	@Override
	public FilterInfo deactivateFilter(String filterId, int revision) throws Exception {
		Connection connection = dataSource.getConnection();
		PreparedStatement ps1 = null, ps2 = null;
		
		try{
			connection.setAutoCommit(false);
			ps1 = connection.prepareStatement(SQL + " where filter_id=? and revision=?");
			ps1.setString(1, filterId);
			ps1.setInt(2, revision);
			
			FilterInfo filterInfo = null;
			ResultSet r = ps1.executeQuery();
			
			if(r.next()){
				filterInfo = buildFilterInfo(r);
				if(!filterInfo.isCanary() && !filterInfo.isActive()){
                    throw new Exception("Filter must be canary or active to deactivate" + filterId + "revision:" + revision);
				}
			}else{
                throw new Exception("Filter not Found " + filterId + "revision:" + revision);
			}
			
			ps2 = connection.prepareStatement("update " + filterTable + " set is_active=?,is_canary=? where filter_id=? and revision=?");
			ps2.setBoolean(1, false);
			ps2.setBoolean(2, false);
			ps2.setString(3, filterId);
			ps2.setInt(4, revision);
			
			ps2.executeUpdate();
			
			connection.commit();
		}finally{
			try{
				if(ps2 !=null){
					ps2.close();
				}
				
				if(ps1 != null){
					ps1.close();
				}
			}finally{
				connection.close();
			}
		}

        return getFilter(filterId, revision);
	}

	@Override
	public FilterInfo addFilter(String filterCode, String filterType, String filterName,
			String filterDisablePropertyName, String filterOrder) throws Exception {

		String filterId = buildFilterId(filterType, filterName);
		int revision = 1;
		Connection connection = dataSource.getConnection();
		PreparedStatement  ps1 = null,ps2=null;
		
		try{
			connection.setAutoCommit(false);
			
			ps1 = connection.prepareStatement("select max(revision) from " + filterTable + " where filter_id=?");
			ps1.setString(1, filterId);
			
			ResultSet r = ps1.executeQuery();
			
			if(r.next()){
				revision = r.getInt(1) + 1;
			}
			
			ps2 = connection.prepareStatement("insert into " + filterTable +"(filter_id,revision,create_time,is_active,is_canary,filter_code,filter_type,filter_name,disable_property_name,filter_order,application_name) values(?,?,?,?,?,?,?,?,?,?,?)");
			ps2.setString(1, filterId);
			ps2.setInt(2, revision);
			ps2.setTimestamp(3, new Timestamp(new Date().getTime()));
			ps2.setBoolean(4, false);
			ps2.setBoolean(5, false);
			ps2.setString(6, filterCode);
			ps2.setString(7, filterType);
			ps2.setString(8, filterName);
			ps2.setString(9, filterDisablePropertyName);
			ps2.setString(10, filterOrder);
			ps2.setString(11, applicationName);
			
			ps2.executeUpdate();
			
			connection.commit();
		}finally{
			try{
			if(ps2 != null){
				ps2.close();
			}
			
			if(ps1 != null){
				ps1.close();
			}
			}finally{
				connection.close();
			}
		}

		return getFilter(filterId, revision);
	}

    @Override
    public String getFilterIdsRaw(String index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<String> getFilterIdsIndex(String index) {
        throw new UnsupportedOperationException();
    }

	private FilterInfo buildFilterInfo(ResultSet r) throws SQLException {
		return new FilterInfo(r.getString(1), r.getInt(2), r.getDate(3), r.getBoolean(4), r.getBoolean(5),
				r.getString(6), r.getString(7), r.getString(8), r.getString(9), r.getString(10), r.getString(11));
	}

	private String buildFilterId(String filterType, String filterName) {
		return FilterInfo.buildFilterId(applicationName, filterType, filterName);
	}

	@PostConstruct
	@Override
	public void close() {
		this.dataSource.close();
	}

}
