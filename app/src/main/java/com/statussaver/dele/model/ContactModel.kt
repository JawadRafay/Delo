package com.statussaver.dele.model

import android.graphics.drawable.Icon

class ContactModel(){
    var ticker:String? = null
    var name:String? = null
    var logo:String? = null
    var text:String? = null
    var date:String? = null
    var type:String? = null
    var time:Long = 0
    var id:Int = 0
    var count:Int = 0
    lateinit var icon:Icon

    constructor(ticker:String?, name:String?, logo:String?, text:String?, time:Long, type:String?) : this(){
        this.ticker = ticker
        this.name = name
        this.logo = logo
        this.text = text
        this.time = time
        this.type = type
    }
}