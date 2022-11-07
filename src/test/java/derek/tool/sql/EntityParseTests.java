//package derek.tool.sql;
//
//import derek.tool.sql.dialect.DatabaseInfo;
//import lombok.Data;
//import org.hibernate.boot.internal.BootstrapContextImpl;
//import org.hibernate.boot.internal.InFlightMetadataCollectorImpl;
//import org.hibernate.boot.internal.MetadataBuilderImpl;
//import org.hibernate.boot.internal.MetadataBuildingContextRootImpl;
//import org.hibernate.boot.model.naming.*;
//import org.hibernate.boot.registry.StandardServiceInitiator;
//import org.hibernate.boot.registry.StandardServiceRegistry;
//import org.hibernate.boot.registry.classloading.internal.ClassLoaderServiceImpl;
//import org.hibernate.boot.registry.internal.BootstrapServiceRegistryImpl;
//import org.hibernate.boot.registry.internal.StandardServiceRegistryImpl;
//import org.hibernate.boot.registry.selector.internal.StrategySelectorImpl;
//import org.hibernate.boot.registry.selector.spi.StrategySelector;
//import org.hibernate.cache.internal.NoCachingRegionFactory;
//import org.hibernate.cache.spi.RegionFactory;
//import org.hibernate.cfg.UniqueConstraintHolder;
//import org.hibernate.cfg.annotations.TableBinder;
//import org.hibernate.dialect.Dialect;
//import org.hibernate.dialect.MySQL5Dialect;
//import org.hibernate.engine.config.internal.ConfigurationServiceImpl;
//import org.hibernate.engine.config.spi.ConfigurationService;
//import org.hibernate.engine.jdbc.dialect.internal.StandardDialectResolver;
//import org.hibernate.engine.jdbc.env.internal.JdbcEnvironmentImpl;
//import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
//import org.hibernate.id.factory.internal.DefaultIdentifierGeneratorFactory;
//import org.hibernate.id.factory.spi.MutableIdentifierGeneratorFactory;
//import org.hibernate.mapping.Table;
//import org.hibernate.service.internal.ProvidedService;
//import org.hibernate.type.BasicType;
//import org.hibernate.type.BasicTypeRegistry;
//import org.jetbrains.annotations.NotNull;
//import org.junit.jupiter.api.Test;
//
//import java.lang.reflect.Field;
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//
///**
// * @author Derek
// * @date 2022/6/17
// */
//public class EntityParseTests {
//
//    @Test
//    public void testTypeMapping() throws Exception {
//        StandardDialectResolver dialectResolver = new StandardDialectResolver();
//        DatabaseInfo dialectInfo = new DatabaseInfo("MySQL", 8, 0);
//        Dialect dialect = dialectResolver.resolveDialect(dialectInfo);
//        BasicTypeRegistry basicTypeRegistry = new BasicTypeRegistry();
//        Field[] declaredFields = IssueEntity.class.getDeclaredFields();
//        for (Field declaredField : declaredFields) {
//            String fieldName = declaredField.getName();
//            String typeClassName = declaredField.getType().getName();
//            BasicType type = basicTypeRegistry.getRegisteredType(typeClassName);
//            if (type == null) {
//                System.out.println("类型不存在" + typeClassName);
//                continue;
//            }
//            int code = type.sqlTypes(null)[0];
//            String sqlType = dialect.getTypeName(code, 100000, 0, 0);
//            System.err.println("字段：" + fieldName + ";\t\tjava类型：" + typeClassName + ";\t\tsql类型：" + sqlType);
//        }
//
//    }
//
//    @NotNull
//    private StandardServiceRegistry initServiceRegistry(HashMap<Object, Object> configValues) {
//        BootstrapServiceRegistryImpl bootstrapServiceRegistry = new BootstrapServiceRegistryImpl();
//        List<StandardServiceInitiator> serviceInitiators = new ArrayList<>();
//        List<ProvidedService> providedServices = new ArrayList<>();
//
//        ConfigurationServiceImpl configurationService = new ConfigurationServiceImpl(configValues);
//        ProvidedService configurationServiceProvidedService =
//                new ProvidedService<>(ConfigurationService.class, configurationService);
//        providedServices.add(configurationServiceProvidedService);
//
//        NoCachingRegionFactory regionFactory = new NoCachingRegionFactory();
//        ProvidedService regionFactoryProvidedService =
//                new ProvidedService(RegionFactory.class, regionFactory);
//        providedServices.add(regionFactoryProvidedService);
//
//        ClassLoaderServiceImpl classLoaderService = new ClassLoaderServiceImpl();
//        StrategySelectorImpl strategySelector = new StrategySelectorImpl(classLoaderService);
//        strategySelector.registerStrategyImplementor(ImplicitNamingStrategy.class, "legacy-hbm",
//                ImplicitNamingStrategyLegacyHbmImpl.class);
//        strategySelector.registerStrategyImplementor(ImplicitNamingStrategy.class, "default",
//                ImplicitNamingStrategyJpaCompliantImpl.class);
//        strategySelector.registerStrategyImplementor(ImplicitNamingStrategy.class, "legacy-jpa",
//                ImplicitNamingStrategyLegacyJpaImpl.class);
//        strategySelector.registerStrategyImplementor(ImplicitNamingStrategy.class, "jpa",
//                ImplicitNamingStrategyJpaCompliantImpl.class);
//        strategySelector.registerStrategyImplementor(ImplicitNamingStrategy.class,
//                "component-path", ImplicitNamingStrategyComponentPathImpl.class);
//        ProvidedService strategySelectorProvidedService =
//                new ProvidedService(StrategySelector.class, strategySelector);
//        providedServices.add(strategySelectorProvidedService);
//
//        MutableIdentifierGeneratorFactory mutableIdentifierGeneratorFactory =
//                new DefaultIdentifierGeneratorFactory(true);
//        ProvidedService MutableIdentifierGeneratorFactoryProvidedService =
//                new ProvidedService(MutableIdentifierGeneratorFactory.class, mutableIdentifierGeneratorFactory);
//        providedServices.add(MutableIdentifierGeneratorFactoryProvidedService);
//
////生成服务注册器
//        StandardServiceRegistryImpl serviceRegistry = new StandardServiceRegistryImpl(bootstrapServiceRegistry,
//                serviceInitiators, providedServices, configValues);
//
//        JdbcEnvironment jdbcEnvironment = new JdbcEnvironmentImpl(serviceRegistry, new MySQL5Dialect());
//        ProvidedService JdbcEnvironmentProvidedService = new ProvidedService(JdbcEnvironment.class, jdbcEnvironment);
//        providedServices.add(JdbcEnvironmentProvidedService);
//
//
//        return serviceRegistry;
//    }
//
//    @Test
//    public void testMetadata() throws Exception {
//        HashMap<Object, Object> configValues = new HashMap<>();
//        configValues.put("hibernate.dialect", "org.hibernate.dialect.MySQL5InnoDBDialect");
//
//
//        StandardServiceRegistry serviceRegistry = initServiceRegistry(configValues);
//
//        MetadataBuilderImpl.MetadataBuildingOptionsImpl options =
//                new MetadataBuilderImpl.MetadataBuildingOptionsImpl(serviceRegistry);
//        BootstrapContextImpl bootstrapContext = new BootstrapContextImpl(serviceRegistry, options);
//        InFlightMetadataCollectorImpl inFlightMetadataCollector = new InFlightMetadataCollectorImpl(bootstrapContext,
//                options);
//        MetadataBuildingContextRootImpl metadataBuildingContextRoot =
//                new MetadataBuildingContextRootImpl(bootstrapContext, options, inFlightMetadataCollector);
//        String schema = "";
//        String catalog = "";
//        Identifier logicalName = new Identifier(IssueEntity.class.getName(), false);
//        List<UniqueConstraintHolder> uniqueConstraints = new ArrayList<>();
//        Table table = TableBinder.buildAndFillTable(
//                schema,
//                catalog,
//                logicalName,
//                false,
//                uniqueConstraints,
//                null,
//                null,
//                metadataBuildingContextRoot,
//                null,
//                null
//        );
//        System.out.println(table);
//    }
//
//
//    @Data
//    public static class IssueEntity {
//
//        private String name;
//
//        private Long id;
//
//        private Integer num;
//
//        private Short snum;
//
//        private Boolean isDelete;
//
//        private BigDecimal percent;
//
//        private LocalDateTime createTime;
//
//        private Date updateTime;
//
//        private String content;
//    }
//
//
//}
