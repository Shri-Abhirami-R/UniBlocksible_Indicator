package com.scanner.app.tracking

import android.R.attr.*
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.BarcodeEncoder
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.scanner.app.tracking.data.model.ManufacturerDetail
import com.scanner.app.tracking.data.model.Product
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    var artNameText: EditText ? =null
    var creatorText: EditText ? =null
    var countryText: EditText ? =null
    var paintingText: EditText ? =null
    var widthText: EditText ? =null
    var heightText: EditText ? =null
    var dimensionText: EditText ? =null
    var quantityText: EditText ? =null
    var barcodeImage: ImageView ? =null
    var shareImage: ImageView ? =null
    var bitmap :Bitmap ? =null
    var product: Product? = null

   private lateinit var database: FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        artNameText = findViewById(R.id.art_name_tv)
        creatorText = findViewById(R.id.creator_name_tv)
        countryText = findViewById(R.id.country_name_tv)
        paintingText = findViewById(R.id.painting_type_tv)
        widthText= findViewById(R.id.width_tv)
        heightText = findViewById(R.id.height_tv)
        dimensionText = findViewById(R.id.dimension_tv)
        quantityText = findViewById(R.id.quantity_tv)
        barcodeImage = findViewById(R.id.qr_code_imageview)
        shareImage = findViewById(R.id.share_imageview)

       database = Firebase.firestore
    }

    fun scanBarcode(view: android.view.View) {
        barcodeLauncher.launch(ScanOptions())
    }

    private val barcodeLauncher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents == null) {
            val originalIntent = result.originalIntent
            if (originalIntent == null) {
                Log.d("MainActivity", "Cancelled scan")
                Toast.makeText(this@MainActivity, "Cancelled", Toast.LENGTH_LONG).show()
            } else if (originalIntent.hasExtra(Intents.Scan.MISSING_CAMERA_PERMISSION)) {
                Log.d(
                    "MainActivity",
                    "Cancelled scan due to missing camera permission"
                )
                Toast.makeText(
                    this@MainActivity,
                    "Cancelled due to missing camera permission",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Log.d("MainActivity", "Scanned")
            try {
                val type = object : TypeToken<ArrayList<Product>>() {}.type
                val product = Gson().fromJson<Product>(result.contents, type)
                getData(product)
            }catch (e: TypeCastException){
                showFakeProductDialog()
            }
           /* Toast.makeText(
                this@MainActivity,
                "Scanned: " + result.contents,
                Toast.LENGTH_LONG
            ).show()*/
        }
    }

    fun generateBarcode(view: android.view.View) {
        val artName = artNameText?.text.toString()
        val creatorName = creatorText?.text.toString()
        val countryName = countryText?.text.toString()
        val paintingType  = paintingText?.text.toString()
        val sizeWidth = widthText?.text.toString()
        val sizeHeight = heightText?.text.toString()
        val dimension = dimensionText?.text.toString()
        val quantity = quantityText?.text.toString()
        var productCreatedTime = ""
        productCreatedTime = getISTTime("yyyy-MM-dd HH:mm:ss")
        val manufactureDetailsList = ArrayList<ManufacturerDetail>()
        val manufactureDetails = ManufacturerDetail()
        manufactureDetails.manufacturerName = creatorText?.text.toString()
        manufactureDetails.createdTime = productCreatedTime
        manufactureDetailsList.add(manufactureDetails)

        val product = Product(artName, creatorName, countryName, paintingType, sizeWidth, sizeHeight, dimension, quantity, productCreatedTime, productCreatedTime, manufactureDetailsList)

        /*val product = hashMapOf(
            "art_name" to artName,
            "creator_name" to creatorName,
            "country_name" to countryName,
            "painting_yype" to paintingType,
            "size_width" to sizeWidth,
            "size_height" to sizeHeight,
            "dimension" to dimension,
            "quantity" to quantity
        )*/

        val jsonText = Gson().toJson(product)

        //val barcodeText: String = "Art Name : ${artName}, Creator Name : ${creatorName}, Counrty : $countryName, Painting Type : $paintingType, Width : $sizeWidth, Height : $sizeHeight, Dimension : $dimension, Quantity : $quantity"

        try {
            bitmap = BarcodeEncoder().encodeBitmap(jsonText, BarcodeFormat.QR_CODE, 400, 400)
            barcodeImage?.setImageBitmap(bitmap)

            try {
                val cachePath = File(getCacheDir(), "images")
                cachePath.mkdirs() // don't forget to make the directory
                val stream =
                    FileOutputStream(cachePath.toString() + "/image.png") // overwrites this image every time
                bitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)
                stream.close()

                saveData(product)

                shareImage?.visibility = View.VISIBLE
                shareImage?.setOnClickListener {
                    shareBarcode()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } catch (e: WriterException) {
            e.printStackTrace()
        }
    }

    //fun saveData(product: HashMap<String, String>) {
    fun saveData(product: Product) {
        database.collection("products").add(product)
            .addOnSuccessListener {documentReference ->
                Log.d("firestore", "DocumentSnapshot added with ID: ${documentReference.id}")

            }
            .addOnFailureListener {e ->
                Log.w("firestore", "Error adding document", e)
            }
    }

    fun getData(product: Product){
        this.product = product
        database.collection("products")
            .whereEqualTo("artName", product.artName)
            .whereEqualTo("creatorName", product.creatorName)
            .whereEqualTo("dimension", product.dimension)
            .whereEqualTo("countryName", product.countryName)
            .get()
            .addOnSuccessListener { result ->
                var data = ""
                for (document in result) {
                    data = document.data.toString()
                    AlertDialog.Builder(this)
                        .setTitle("Product Details")
                        .setCancelable(true)
                        .setMessage(data)
                        .setPositiveButton("Ok") { dialog, which ->
                            dialog.dismiss()
                        }
                        //.setNegativeButton(R.string.no, null)
                        .create()
                        .show()

                    val intent = Intent(this, ProductActivity::class.java)
                    intent.putExtra("product", Gson().toJson(this.product))
                    startActivity(intent)
                }
                if(data.isNullOrEmpty())
                    showFakeProductDialog()
            }
            .addOnFailureListener {
                showFakeProductDialog()
            }

    }

    fun showFakeProductDialog(){
        AlertDialog.Builder(this)
            .setTitle("Product Details")
            .setCancelable(true)
            .setMessage("Fake Product")
            .setPositiveButton("Ok") { dialog, which ->
                dialog.dismiss()
            }
            //.setNegativeButton(R.string.no, null)
            .create()
            .show()
    }

    fun shareBarcode(){
        val imagePath: File = File(getCacheDir(), "images")
        val newFile = File(imagePath, "image.png")
        val contentUri: Uri? =
            FileProvider.getUriForFile(this, "com.scanner.app.tracking.fileprovider", newFile)

        if (contentUri != null) {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // temp permission for receiving app to read this file
            shareIntent.setDataAndType(contentUri, contentResolver.getType(contentUri))
            shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri)
            startActivity(Intent.createChooser(shareIntent, "Choose an app"))
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