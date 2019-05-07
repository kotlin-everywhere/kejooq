package org.kotlin.everywhere.kejooq

import org.jooq.DSLContext
import org.jooq.Record
import org.jooq.SelectLimitStep
import java.lang.Integer.max
import java.lang.Integer.min


data class Pagination<out T>(val first: Int, val total: Int, val last: Int, val page: Int, val items: List<T>,
                             internal val start: Int,
                             val prev: Int, val hasPrev: Boolean, val next: Int, val hasNext: Boolean,
                             val navPrev: Int, val hasNavPrev: Boolean, val navNext: Int,
                             val hasNavNext: Boolean, val pages: List<Int>) {

    val itemsIndexed by lazy { items.mapIndexed { i, e -> Entry(total - start - i, e) } }


    companion object {
        fun <R : Record> of(ctx: DSLContext,
                            query: SelectLimitStep<R>,
                            forPage: Int = 1,
                            perPage: Int = 10,
                            perNav: Int = 10,
                            countQuery: SelectLimitStep<R>? = null
        ): Pagination<R> {
            val total = ctx.fetchCount(countQuery ?: query)

            val last = if (total == 0) 1 else ceilDivision(total, perPage)
            val page = max(min(last, forPage), 1)

            val prev = max(page - 1, 1)
            val hasPrev = prev != page
            val next = min(page + 1, last)
            val hasNext = next != page

            val navHead = perNav * (ceilDivision(page, perNav) - 1) + 1
            val navTail = min(last, navHead + perNav - 1)

            val navPrev = max(page - perNav, 1)
            val hasNavPrev = navPrev < navHead
            val navNext = min(page + perNav, last)
            val hasNavNext = navNext > navTail

            val pages = (navHead..navTail).toList()

            val start = (page - 1) * perPage
            val items = query
                    .limit(start, perPage)
                    .fetch()

            return Pagination(1, total, last, page, items, start, prev, hasPrev, next, hasNext, navPrev, hasNavPrev, navNext, hasNavNext, pages)
        }
    }

    data class Entry<out T>(val index: Int, val item: T)
}

fun <R : Record> SelectLimitStep<R>.paginate(
        ctx: DSLContext,
        forPage: Int = 1,
        perPage: Int = 10,
        perNav: Int = 10
): Pagination<R> = Pagination.of(ctx = ctx, query = this, forPage = forPage, perPage = perPage, perNav = perNav)

fun <T, U> Pagination<T>.map(mapper: (T) -> U): Pagination<U> {
    return Pagination(
            first = first,
            total = total,
            last = last,
            page = page,
            items = items.map(mapper),
            start = start,
            prev = prev,
            hasPrev = hasPrev,
            next = next,
            hasNext = hasNext,
            navPrev = navPrev,
            hasNavPrev = hasNavPrev,
            navNext = navNext,
            hasNavNext = hasNavNext,
            pages = pages
    )
}

private fun ceilDivision(x: Int, y: Int): Int {
    return Math.ceil(x.toDouble() / y).toInt()
}
