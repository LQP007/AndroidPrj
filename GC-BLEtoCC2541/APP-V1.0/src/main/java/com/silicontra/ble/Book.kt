package com.silicontra.ble

/**
 * Created by Jay on 2015/9/22 0022.
 */
class Book {
    private var bName: String? = null
    private var bAuthor: String? = null

    constructor() {}

    constructor(bName: String, bAuthor: String) {
        this.bName = bName
        this.bAuthor = bAuthor
    }

    fun getbName(): String? {
        return bName
    }

    fun getbAuthor(): String? {
        return bAuthor
    }

    fun setbName(bName: String) {
        this.bName = bName
    }

    fun setbAuthor(bAuthor: String) {
        this.bAuthor = bAuthor
    }
}
