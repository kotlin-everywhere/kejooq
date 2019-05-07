package org.kotlin.everywhere.kejooq

import org.kotlin.everywhere.kejooq.database.tables.User.USER
import kotlin.test.Test
import kotlin.test.assertEquals


class TestPagination {

    @Test
    fun testNormalizePage() {
        val getUsers = { page: Int ->
            db { ctx ->
                ctx.select().from(USER).where(USER.ID.lt(0))
                        .paginate(ctx = ctx, forPage = page)
            }
        }

        assertEquals(1, getUsers(1).page)

        // overflow 방지
        assertEquals(1, getUsers(10).page)

        // underflow 방지
        assertEquals(1, getUsers(-1).page)
    }
}