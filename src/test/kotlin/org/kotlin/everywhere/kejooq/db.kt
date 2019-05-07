package org.kotlin.everywhere.kejooq

import org.h2.Driver
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL

fun <T> db(run: (DSLContext) -> T): T {
    val dbSql = Any::class.java
            .getResourceAsStream("/db.sql")
            .reader()
            .readText()

    return Driver()
            .connect("jdbc:h2:mem:test-jooq-tools", null)
            .use {
                val create = DSL.using(it, SQLDialect.H2)
                create.execute(dbSql)
                run(create)
            }
}
