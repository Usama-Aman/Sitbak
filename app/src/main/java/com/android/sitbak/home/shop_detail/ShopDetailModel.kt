package com.android.sitbak.home.shop_detail

import java.math.BigInteger

data class ShopDetailModel(

    val status: Boolean,
    val message: String,
    val data: List<ShopDetailData>,
//    val categories: List<ShopDetailCategories>
)

data class ShopDetailData(
    val id: BigInteger,
    val title: String,
//    val body_html: String,
    val vendor: String,
    val product_type: String,
    val created_at: String,
    val handle: String,
    val updated_at: String,
    val published_at: String,
//    val template_suffix: String,
    val status: String,
//    val published_scope: String,
    val tags: String,
    val admin_graphql_api_id: String,
    val variants: List<ShopDetailVariants>?,
//    val options: List<ShopDetailOptions>,
//    val images: List<ShopDetailImages>,
    val image: ShopDetailImage?,
    val isHeader: Boolean = false,
)

data class ShopDetailVariants(
    val id: String,
    val product_id: String,
    val title: String,
    val price: Double,
    val sku: String,
    val position: Int,
    val inventory_policy: String,
    val compare_at_price: String,
    val fulfillment_service: String,
    val inventory_management: String,
    val option1: String,
    val option2: String,
    val option3: String,
    val created_at: String,
    val updated_at: String,
    val taxable: Boolean,
    val barcode: String,
    val grams: Int,
    val image_id: String,
    val weight: Int,
    val weight_unit: String,
    val inventory_item_id: String,
    val inventory_quantity: Int,
    val old_inventory_quantity: Int,
    val requires_shipping: Boolean,
    val admin_graphql_api_id: String
)

data class ShopDetailImages(

    val id: String,
    val product_id: String,
    val position: Int,
    val created_at: String,
    val updated_at: String,
    val alt: String,
    val width: Int,
    val height: Int,
    val src: String,
    val variant_ids: List<String>,
    val admin_graphql_api_id: String
)


data class ShopDetailImage(
    val id: String,
    val product_id: String,
    val position: Int,
    val created_at: String,
    val updated_at: String,
    val alt: String,
    val width: Int,
    val height: Int,
    val src: String,
    val variant_ids: List<String>,
    val admin_graphql_api_id: String
)


data class ShopDetailOptions(
    val id: String,
    val product_id: String,
    val name: String,
    val position: Int,
    val values: List<String>
)

data class ShopDetailCategories(
    val id: Int,
    val category_title: String,
    val category_description: String,
    val image: String,
    val image_path: String
)