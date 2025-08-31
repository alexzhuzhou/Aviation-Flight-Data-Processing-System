package com.example.config;

import org.hibernate.dialect.function.SQLFunctionTemplate;
import org.hibernate.spatial.dialect.oracle.OracleSpatial10gDialect;
import org.hibernate.type.StandardBasicTypes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomOracle10gDialect extends OracleSpatial10gDialect {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomOracle10gDialect.class);

    public CustomOracle10gDialect() {
        super();
        logger.info("CustomOracle10gDialect initialized - using custom Oracle dialect for Sigma database");
        registerFunction(
                "regexp_like", new SQLFunctionTemplate(StandardBasicTypes.BOOLEAN,
                        "(case when (regexp_like(?1, ?2)) then 1 else 0 end)")
        );
        logger.debug("CustomOracle10gDialect: regexp_like function registered");
    }
}
