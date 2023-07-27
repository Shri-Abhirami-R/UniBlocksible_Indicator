package com.scanner.app.tracking.data.model

data class Product(val artName: String? = null, val creatorName: String? = null, val countryName: String? = null
                   , val paintingType: String? = null, val sizeWidth: String? = null, val sizeHeight: String? = null
                   , val dimension: String? = null, val quantity: String? = null, var createdTime: String? = null, var updatedTime: String? = null, var manufacturerDetail: ArrayList<ManufacturerDetail>? = null)
