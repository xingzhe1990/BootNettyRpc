package io.github.forezp.netty.rpc.core.config;


import io.github.forezp.netty.rpc.core.config.condition.NettyServerCondition;
import io.github.forezp.netty.rpc.core.common.constant.ConfigConstants;
import io.github.forezp.netty.rpc.core.common.enums.LoadBalancerType;
import io.github.forezp.netty.rpc.core.protocol.client.ResponseHandler;
import io.github.forezp.netty.rpc.core.loadbalance.LoadBalanceExcutor;
import io.github.forezp.netty.rpc.core.loadbalance.RandomLoadBalanceExecutor;
import io.github.forezp.netty.rpc.core.loadbalance.RoundRobinLoadBalaceExecutor;
import io.github.forezp.netty.rpc.core.protocol.server.RequestHandler;
import io.github.forezp.netty.rpc.core.protocol.client.RequestInterceptor;
import io.github.forezp.netty.rpc.core.common.container.CacheContainer;
import io.github.forezp.netty.rpc.core.common.container.ExcutorContainer;
import io.github.forezp.netty.rpc.core.common.entity.AppEntity;
import io.github.forezp.netty.rpc.core.protocol.client.NettyClientExcutor;
import io.github.forezp.netty.rpc.core.protocol.server.NettyServerExcutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.*;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.util.StringUtils;

import static io.github.forezp.netty.rpc.core.common.constant.ConfigConstants.*;

/**
 * ${DESCRIPTION}
 *
 * @author fangzhipeng
 * create 2018-05-25
 **/

@Configuration
@ComponentScan(basePackages = {"io.github.forezp"})
public class NettyRpcConfig {

    @Autowired
    ConfigurableEnvironment env;

    @Bean
    CommonProperties commonProperties() {
        CommonProperties properties = new CommonProperties();
        setCommonProperties( properties );
        return properties;
    }

    @Bean
    NettyRpcProperties nettyRpcProperties() {
        NettyRpcProperties properties = new NettyRpcProperties();
        properties.setCommonProperties( commonProperties() );
        return properties;
    }

    @Bean
    FactorConfig factorConfig() {
        FactorConfig factorConfig = new FactorConfig();
        factorConfig.setThreadPoolFactory( commonProperties() );
        return factorConfig;
    }

    @Bean
    CacheContainer cacheContainer() {
        return new CacheContainer();
    }

    @Bean
    ExcutorContainer excutorContainer() {
        return new ExcutorContainer();
    }

    @Bean
    @Conditional(NettyServerCondition.class)
    public NettyServerExcutor nettyServerExcutor() {
        AppEntity appEntity = nettyRpcProperties().getServer();
        NettyServerExcutor excutor = new NettyServerExcutor();
        excutor.setNettyRpcProperties( nettyRpcProperties() );
        excutor.setCacheContainer( cacheContainer() );
        excutor.setExcutorContainer( excutorContainer() );
        excutorContainer().setServerExcutor( excutor );
        try {
            excutor.start( appEntity );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return excutor;
    }

    @Bean
    public NettyClientExcutor nettyClientExcutor() {
        NettyClientExcutor excutor = new NettyClientExcutor();
        excutor.setCacheContainer( cacheContainer() );
        excutor.setNettyRpcProperties( nettyRpcProperties() );
        excutor.setExcutorContainer( excutorContainer() );
        excutorContainer().setClientExcutor( excutor );
        return excutor;
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        RequestInterceptor requestInterceptor = new RequestInterceptor();
        requestInterceptor.setCacheContainer( cacheContainer() );
        requestInterceptor.setNettyRpcProperties( nettyRpcProperties() );
        requestInterceptor.setExcutorContainer( excutorContainer() );
        excutorContainer().setRequestInterceptor( requestInterceptor );
        return requestInterceptor;
    }

    @Bean
    public RequestHandler requestHandler() {
        RequestHandler serverRequestHandler = new RequestHandler();
        serverRequestHandler.setCacheContainer( cacheContainer() );
        serverRequestHandler.setNettyRpcProperties( nettyRpcProperties() );
        serverRequestHandler.setExcutorContainer( excutorContainer() );
        excutorContainer().setServerRequestHandler( serverRequestHandler );
        return serverRequestHandler;
    }

    @Bean
    public ResponseHandler responseHandler() {
        ResponseHandler responseHandler = new ResponseHandler();
        responseHandler.setCacheContainer( cacheContainer() );
        responseHandler.setNettyRpcProperties( nettyRpcProperties() );
        responseHandler.setExcutorContainer( excutorContainer() );
        excutorContainer().setResponseHandler( responseHandler );
        return responseHandler;
    }

    @Bean
    public LoadBalanceExcutor loadBalanceExcutor() {
        LoadBalanceExcutor loadBalanceExcutor;
        if (StringUtils.isEmpty( nettyRpcProperties().getCommonProperties().getLoadBalanceType() )
                || nettyRpcProperties().getCommonProperties().getLoadBalanceType().equals( LoadBalancerType.RANDOM.getType() )) {
            loadBalanceExcutor = new RandomLoadBalanceExecutor();
        } else if (nettyRpcProperties().getCommonProperties().getLoadBalanceType().equals( LoadBalancerType.ROUND_ROBIN.getType() )) {
            loadBalanceExcutor = new RoundRobinLoadBalaceExecutor();
        } else {
            loadBalanceExcutor = new RandomLoadBalanceExecutor();
        }
        loadBalanceExcutor.setCacheContainer( cacheContainer() );
        loadBalanceExcutor.setNettyRpcProperties( nettyRpcProperties() );
        loadBalanceExcutor.setExcutorContainer( excutorContainer() );
        excutorContainer().setLoadBalanceExcutor( loadBalanceExcutor );
        return loadBalanceExcutor;
    }


    private void setCommonProperties(CommonProperties properties) {
        String eurekaEnable = env.getProperty( NETTY_EUREKA_ENABLE, NETTY_EUREKA_ENABLE_DEFAULT );
        String loadBalanceType = env.getProperty( ConfigConstants.NETTY_LOADBALANCE_TYPE, "" );
        properties.setEurekaEnable( eurekaEnable );
        properties.setLoadBalanceType( loadBalanceType );

        String serverPoolCoreSize = env.getProperty( SERVER_POOL_CORE_SIZE, SERVER_POOL_CORE_SIZE_DEFAULT );
        String serverPoolMaxSize = env.getProperty( SERVER_POOL_MAX_SIZE, SERVER_POOL_MAX_SIZE_DEFAULT );
        String serverPoolQunueType = env.getProperty( SERVER_POOL_QUEUE_TYPE, SERVER_POOL_QUEUE_TYPE_DEFAULT );
        String serverPoolQunueSize = env.getProperty( SERVER_POOL_QUEUE_SIZE, SERVER_POOL_QUEUE_SIZE_DEFAULT );
        String serverPoolKeepAliveTime = env.getProperty( SERVER_POOL_KEEPALIVE_TIME, SERVER_POOL_KEEPALIVE_TIME_DEFAULT );
        String serverPoolRejectType = env.getProperty( SERVER_POOL_REJECT_TYPE, SERVER_POOL_REJECT_TYPE_DEFAULT );

        properties.setServerPoolCoreSize( Integer.parseInt( serverPoolCoreSize ) );
        properties.setServerPoolMaxSize( Integer.parseInt( serverPoolMaxSize ) );
        properties.setServerPoolQunueType( serverPoolQunueType );
        properties.setServerPoolQunueSize( Integer.parseInt( serverPoolQunueSize ) );
        properties.setServerPoolKeepAliveTime( Long.parseLong( serverPoolKeepAliveTime ) );
        properties.setServerPoolRejectType( serverPoolRejectType );


        String clientPoolCoreSize = env.getProperty( CLIENT_POOL_CORE_SIZE, CLIENT_POOL_CORE_SIZE_DEFAULT );
        String clientPoolMaxSize = env.getProperty( CLIENT_POOL_MAX_SIZE, CLIENT_POOL_MAX_SIZE_DEFAULT );
        String clientPoolQunueType = env.getProperty( CLIENT_POOL_QUEUE_TYPE, CLIENT_POOL_QUEUE_TYPE_DEFAULT );
        String clientPoolQunueSize = env.getProperty( CLIENT_POOL_QUEUE_SIZE, CLIENT_POOL_QUEUE_SIZE_DEFAULT );
        String clientPoolKeepAliveTime = env.getProperty( CLIENT_POOL_KEEPALIVE_TIME, CLIENT_POOL_KEEPALIVE_TIME_DEFAULT );
        String clientPoolRejectType = env.getProperty( CLIENT_POOL_REJECT_TYPE, CLIENT_POOL_REJECT_TYPE_DEFAULT );
        String nettyClientRenewInterval = env.getProperty( NETTY_CLIENT_RENEW_INTERVAL, NETTY_CLIENT_RENEW_INTERVAL_DEFAULT );

        String monitorUrl = env.getProperty( MONITOR_HTTP_URL );
        properties.setClientPoolCoreSize( Integer.parseInt( clientPoolCoreSize ) );
        properties.setClientPoolMaxSize( Integer.parseInt( clientPoolMaxSize ) );
        properties.setClientPoolQunueType( clientPoolQunueType );
        properties.setClientPoolQunueSize( Integer.parseInt( clientPoolQunueSize ) );
        properties.setClientPoolKeepAliveTime( Long.parseLong( clientPoolKeepAliveTime ) );
        properties.setClientPoolRejectType( clientPoolRejectType );
        properties.setNettyClientRenewInterval( nettyClientRenewInterval );
        properties.setMonitorUrl( monitorUrl );

        String httpConnectTimeout = env.getProperty( HTTPCLIENT_CONNCT_TIMEOUT, HTTPCLIENT_CONNCT_TIMEOUT_DEFAULT );
        String httpConnectRequestTimeout = env.getProperty( HTTPCLIENT_CONNCT_REQUEST_TIMEOUT, HTTPCLIENT_CONNCT_REQUEST_TIMEOUT_DEFAULT );
        String httpSocketTimeout = env.getProperty( HTTPCLIENT_SOCKET_TIMEOUT, HTTPCLIENT_SOCKET_TIMEOUT_DEFAULT );
        String httpSendBufSize = env.getProperty( HTTPCLIENT_SEDBUFSIZE, HTTPCLIENT_SEDBUFSIZE_DEFAULT );
        String httpRcvBufSize = env.getProperty( HTTPCLIENT_RCV_BUFSIZE, HTTPCLIENT_RCV_BUFSIZE_DEFAULT );
        String httpBackLogSize = env.getProperty( HTTPCLIENT_BACK_LOG_SIZE, HTTPCLIENT_BACK_LOG_SIZE_DEFAULT );
        String httpMaxTotal = env.getProperty( HTTPCLIENT_MAX_TOTAL, HTTPCLIENT_MAX_TOTAL_DEFAULT );

        properties.setHttpConnectTimeout( httpConnectTimeout );
        properties.setHttpConnectRequestTimeout( httpConnectRequestTimeout );
        properties.setHttpSocketTimeout( httpSocketTimeout );
        properties.setHttpRcvBufSize( httpRcvBufSize );
        properties.setHttpSendBufSize( httpSendBufSize );
        properties.setHttpBackLogSize( httpBackLogSize );
        properties.setHttpMaxTotal( httpMaxTotal );

        String monitorRedisExpire = env.getProperty( MONITOR_REDIS_EXPIRE, MONITOR_REDIS_EXPIRE_DEFAULT );
        String monitorRedisExpireTimeUnit = env.getProperty( MONITOR_REDIS_EXPIRE_TIMEUNIT, MONITOR_REDIS_EXPIRE_TIMEUNIT_DEFAULT );

        properties.setMonitorRedisExpire( monitorRedisExpire );
        properties.setMonitorRedisExpireTimeUnit( monitorRedisExpireTimeUnit );

        /**
         * 设置邮箱相关的
         */
        String mailEnable = env.getProperty( MONITOR_MAIL_ENABLE );
        String mailHost = env.getProperty( MAIL_HOST );
        String mailUserName = env.getProperty( MAIL_USER_NAME );
        String mailUserPassword = env.getProperty( MAIL_USER_PASSWORD );
        String isMailEnableSsl = env.getProperty( MAIL_IS_ENABLE_SSL );
        String mailFrom = env.getProperty( MAIL_FROM );
        String mailTo = env.getProperty( MAIL_TO );
        String mailCc = env.getProperty( MAIL_CC );
        String mailBcc = env.getProperty( MAIL_BCC );

        properties.setMailEnable( mailEnable );
        properties.setMailHost( mailHost );
        properties.setMailUserName( mailUserName );
        properties.setMailUserPassword( mailUserPassword );
        properties.setIsMailEnableSsl( isMailEnableSsl );
        properties.setMailFrom( mailFrom );
        properties.setMailTo( mailTo );
        properties.setMailCc( mailCc );
        properties.setMailBcc( mailBcc );


    }
}