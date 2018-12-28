# s2g-zuul
Spring2go定制版Netflix zuul

## 注意！！！

1. 本项目为微服务课程讲解开发，代码仅供学习参考，如需生产化，需要做生产化扩展+严格测试！！！另外请考虑Spring Cloud Zuul。
2. 注意本项目依赖[CAT3.0](https://github.com/dianping/cat)客户端，启动前需要先CAT客户端配置工作，否则Servlet会启不来，步骤如下描述。

## 启动 cat 客户端前的准备工作

1. 创建 `/data/appdatas/cat` 目录

    确保你具有这个目录的读写权限。

2. 创建 `/data/applogs/cat` 目录 (可选)

    这个目录是用于存放运行时日志的，这将会对调试提供很大帮助，同样需要读写权限。

3. 创建 `/data/appdatas/cat/client.xml`，内容如下

    ```xml
    <?xml version="1.0" encoding="utf-8"?>
    <config xmlns:xsi="http://www.w3.org/2001/XMLSchema" xsi:noNamespaceSchemaLocation="config.xsd">
        <servers>
            <server ip="127.0.0.1" port="2280" http-port="8080" />
        </servers>
    </config>
    ```

    > 如果不实际使用CAT，只是验证Zuul功能，则上面CAT服务器地址可以随意填；如果要实际启用CAT服务器，则上面需要填写你的CAT server地址。

注意，上述目录和zuul在要在同一逻辑盘下。
