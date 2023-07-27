package com.scanner.app.tracking

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.scanner.app.tracking.data.model.ManufacturerDetail
import com.scanner.app.tracking.data.model.Product
import com.scanner.app.tracking.data.model.UserDetails
import java.text.SimpleDateFormat
import java.util.*

class ProductActivity : AppCompatActivity() {

    var productString = ""
    var product: Product? = null

    var name: TextView? = null
    var paintingType: TextView? = null
    //var location: TextView? = null
    var clickHereToUpdate: TextView? = null
    var updateManufacturer: TextView? = null
    var updateManufacturerName: TextView? = null
    var productDetailTextview: TextView? = null
    var moreInfo: ImageView? = null
    var updateLayout: LinearLayout? = null
    var titleLayout: LinearLayout? = null
    private lateinit var database: FirebaseFirestore
    var progress: ProgressDialog? = null
    var documentId = ""
    var manufacturerRecyclerView: RecyclerView ? = null
    var userDetailString = ""
    var userDetails: UserDetails? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product)

        name = findViewById(R.id.art_name)
        paintingType = findViewById(R.id.painting_type)
        //location = findViewById(R.id.location_tv)
        clickHereToUpdate = findViewById(R.id.click_here_tv)
        updateManufacturer = findViewById(R.id.update_manufacturer)
        updateLayout = findViewById(R.id.update_layout)
        titleLayout = findViewById(R.id.title_layout)
        updateManufacturerName = findViewById(R.id.update_manufacturer_name)
        manufacturerRecyclerView = findViewById(R.id.manufacturer_rv)
        productDetailTextview = findViewById(R.id.product_detail_title_tv)
       // moreInfo = findViewById(R.id.more_iv)

        userDetailString = intent.getStringExtra("userdetail").toString()
        productString = intent.getStringExtra("product").toString()
        documentId = intent.getStringExtra("id").toString()
        if (productString.isNotEmpty()) {
            val type = object : TypeToken<Product>() {}.type
            product = Gson().fromJson<Product>(productString, type)
        }

        if(userDetailString.isNotEmpty()) {
            val type = object : TypeToken<UserDetails>() {}.type
            userDetails = Gson().fromJson<UserDetails>(userDetailString, type)
        }

        if(userDetails != null){
            if(userDetails?.manufacturer != null && userDetails?.manufacturer.equals("Y")){
                clickHereToUpdate?.visibility = View.VISIBLE
            }else{
                clickHereToUpdate?.visibility = View.GONE
            }
        }

        if (product != null)
            bindData()

        clickHereToUpdate?.setOnClickListener {
            clickHereToUpdate?.visibility = View.GONE
            updateLayout?.visibility = View.VISIBLE
        }

        updateManufacturer?.setOnClickListener {
            if (updateManufacturerName?.text!!.isNotEmpty()) {
                progress = ProgressDialog(this)
                progress?.setCancelable(true)
                progress?.setMessage("Loading...")
                progress?.show()
                saveData()
            } else {
                Toast.makeText(this, "Please Enter Name", Toast.LENGTH_LONG).show()
            }
        }
        productDetailTextview?.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Product Details")
                .setCancelable(true)
                .setMessage(productString)
                .setPositiveButton("Ok") { dialog, which ->
                    dialog.dismiss()
                }
                //.setNegativeButton(R.string.no, null)
                .create()
                .show()
        }

        database = Firebase.firestore
    }

    private fun saveData() {
        /*if(product?.manufacturerDetail!!.isNotEmpty()){
            val manufactureDetails = ManufacturerDetail()
            manufactureDetails.manufacturerName = updateManufacturerName?.text.toString()
            manufactureDetails.createdTime = getISTTime("yyyy-MM-dd HH:mm:ss")
            product?.manufacturerDetail?.add(manufactureDetails)
        }else{
            val manufactureDetails = ManufacturerDetail()
            manufactureDetails.manufacturerName = updateManufacturerName?.text.toString()
            manufactureDetails.createdTime = getISTTime("yyyy-MM-dd HH:mm:ss")
            product?.manufacturerDetail?.add(manufactureDetails)
        }*/
        try {
            val manufactureDetails = ManufacturerDetail()
            manufactureDetails.manufacturerName = updateManufacturerName?.text.toString()
            manufactureDetails.createdTime = getISTTime("yyyy-MM-dd HH:mm:ss")
            product?.manufacturerDetail?.add(manufactureDetails)

            //database.collection("products").add(product!!)

            database.collection("products").document(documentId).set(product!!)
                .addOnSuccessListener { documentReference ->
                    progress?.dismiss()
                    Toast.makeText(this, "Updated Successfully!", Toast.LENGTH_LONG).show()
                }
                .addOnFailureListener { e ->
                    progress?.dismiss()
                    Toast.makeText(this, "Update Failed", Toast.LENGTH_LONG).show()
                }
        } catch (e: Exception) {
            progress?.dismiss()
            Toast.makeText(this, "Update Failed : " + e.message, Toast.LENGTH_LONG).show()
        }
    }

    private fun bindData() {
        name?.text = product?.artName
        paintingType?.text = product?.paintingType
        //location?.text = product?.countryName

        if(product?.manufacturerDetail?.size!! > 0) {
            titleLayout?.visibility = View.VISIBLE
            var manufacturerDetailAdapter = ManufacturerDetailAdapter(product?.manufacturerDetail!!)
            manufacturerRecyclerView?.adapter = manufacturerDetailAdapter

        }
    }

    fun getISTTime(SDF: String): String {
        try {
            val calender = Calendar.getInstance()
            val dateFormat = SimpleDateFormat(SDF)
            dateFormat.timeZone = TimeZone.getTimeZone("GMT+05:30")
            return dateFormat.format(calender.time)
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }
}