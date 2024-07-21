package com.statussaver.dele.utils

import com.statussaver.dele.model.ContactModel

interface RefreshListener {
    fun onRefresh(model: ContactModel?)
}