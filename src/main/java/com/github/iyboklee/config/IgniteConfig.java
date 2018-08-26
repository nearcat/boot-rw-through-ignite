package com.github.iyboklee.config;

import javax.sql.DataSource;
import java.util.List;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.IgniteException;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheAtomicityMode;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.DataRegionConfiguration;
import org.apache.ignite.configuration.DataStorageConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.logger.slf4j.Slf4jLogger;
import org.apache.ignite.spi.communication.tcp.TcpCommunicationSpi;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.iyboklee.cache.BookCache;
import com.github.iyboklee.cache.BookCacheStoreFactory;
import com.github.iyboklee.model.Book;
import com.zaxxer.hikari.HikariDataSource;

import net.sf.log4jdbc.Log4jdbcProxyDataSource;

@Configuration
public class IgniteConfig {

    @Value("#{'${ignite.cluster.nodes}'.split(',')}") private List<String> nodes;

    @Value("${ignite.cache.hotloading}") private int hotLoading;

    @Autowired private DataSourceProperties dataSourceProperties;

    @Bean
    public DataSource dataSource() {
        DataSourceBuilder factory = DataSourceBuilder
                .create(dataSourceProperties.getClassLoader())
                .url(dataSourceProperties.getUrl())
                .username(dataSourceProperties.getUsername())
                .password(dataSourceProperties.getPassword());
        HikariDataSource dataSource = (HikariDataSource) factory.build();
        dataSource.setPoolName("[TEST] MyHikariSource");
        dataSource.setMinimumIdle(1);
        dataSource.setMaximumPoolSize(3);
        return new Log4jdbcProxyDataSource(dataSource);
    }

    @Bean
    IgniteConfiguration igniteConfiguration(DataSource dataSource) {
        IgniteConfiguration igniteCfg = new IgniteConfiguration();
        igniteCfg.setClientMode(false);
        igniteCfg.setPeerClassLoadingEnabled(true);

        // Logger
        igniteCfg.setGridLogger(new Slf4jLogger());
        igniteCfg.setMetricsLogFrequency(1000 * 10);

        // Cluster Discovery
        TcpDiscoverySpi spi  = new TcpDiscoverySpi();
        TcpDiscoveryVmIpFinder ipFinder = new TcpDiscoveryVmIpFinder();
        ipFinder.setAddresses(nodes);
        spi.setIpFinder(ipFinder);
        spi.setSocketTimeout(100);
        igniteCfg.setDiscoverySpi(spi);

        // Cluster Communication
        TcpCommunicationSpi commSpi = new TcpCommunicationSpi();
        commSpi.setConnectTimeout(3000);
        commSpi.setTcpNoDelay(true);
        commSpi.setMessageQueueLimit(10000);
        igniteCfg.setCommunicationSpi(commSpi);

        // Memory Configuration
        DataStorageConfiguration storageCfg = new DataStorageConfiguration();
        DataRegionConfiguration defaultDataRegionCfg = storageCfg.getDefaultDataRegionConfiguration();
        defaultDataRegionCfg.setPersistenceEnabled(false);   // Only Memory
        defaultDataRegionCfg.setMaxSize(1024 * 1024 * 256);  // 256MB
        defaultDataRegionCfg.setMetricsEnabled(true);
        igniteCfg.setDataStorageConfiguration(storageCfg);

        // Cache Configuration
        CacheConfiguration<String, Book> cacheCfg = new CacheConfiguration<>(BookCache.CACHE_NAME);
        cacheCfg.setCacheMode(CacheMode.PARTITIONED);
        cacheCfg.setBackups(1);
        cacheCfg.setAtomicityMode(CacheAtomicityMode.TRANSACTIONAL);
        cacheCfg.setCacheStoreFactory(new BookCacheStoreFactory(dataSource));
        /*cacheCfg.setCacheStoreSessionListenerFactories((Factory<CacheStoreSessionListener>) () -> {
            CacheSpringStoreSessionListener lsnr = new CacheSpringStoreSessionListener();
            lsnr.setDataSource(dataSource);
            return lsnr;
        });*/
        cacheCfg.setReadThrough(true);
        cacheCfg.setWriteThrough(true);
        igniteCfg.setCacheConfiguration(cacheCfg);
        return igniteCfg;
    }

    @Bean(destroyMethod = "close")
    public Ignite igniteInstance(IgniteConfiguration configuration) throws IgniteException {
        Ignite ignite = Ignition.start(configuration);
        ignite.cluster().active(true);
        return ignite;
    }

    @Bean
    public BookCache bookCache(Ignite ignite) {
        IgniteCache<String, Book> cache = ignite.getOrCreateCache(BookCache.CACHE_NAME);
        if (hotLoading > 0)
            cache.loadCache(null, hotLoading);
        return new BookCache(cache);
    }

}
