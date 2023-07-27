package com.scanner.app.tracking

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.zxing.client.android.Intents
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.scanner.app.tracking.data.model.Product
import com.scanner.app.tracking.data.model.UserDetails

class MenuActivity : AppCompatActivity() {

    var username :String = ""
    var addProductButton: Button ? =null
    var scanProductButton: Button ? =null
    private lateinit var database: FirebaseFirestore
    var userDetailString = ""
    var userDetails: UserDetails? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)
        username = intent.getStringExtra("username").toString()
        userDetailString = intent.getStringExtra("userdetail").toString()
        addProductButton = findViewById(R.id.add_product)
        scanProductButton = findViewById(R.id.scan_product)

        database = Firebase.firestore

        if(userDetailString.isNotEmpty()) {
            val type = object : TypeToken<UserDetails>() {}.type
            userDetails = Gson().fromJson<UserDetails>(userDetailString, type)
        }

        if(userDetails != null){
            if(userDetails?.manufacturer != null && userDetails?.manufacturer.equals("Y")){
                addProductButton?.visibility = View.VISIBLE
            }else{
                addProductButton?.visibility = View.GONE
            }
        }

      /*  if(username == "admin@gmail.com"){
            addProductButton?.visibility = View.VISIBLE
        }else{
            addProductButton?.visibility = View.GONE
        }*/

        addProductButton?.setOnClickListener {
            GoToMainActivity()
        }

        scanProductButton?.setOnClickListener {
            /*val jsonString = "{\n" +
                    "  \"updatedTime\": \"2022-03-28 18:24:50\",\n" +
                    "  \"artName\": \"testing\",\n" +
                    "  \"quantity\": \"5\",\n" +
                    "  \"sizeWidth\": \"6\",\n" +
                    "  \"paintingType\": \"test\",\n" +
                    "  \"creatorName\": \"vinodkumar\",\n" +
                    "  \"sizeHeight\": \"8\",\n" +
                    "  \"createdTime\": \"2022-03-28 18:24:50\",\n" +
                    "  \"countryName\": \"India\",\n" +
                    "  \"dimension\": \"3\",\n" +
                    "  \"manufacturerDetail\": [\n" +
                    "    {\n" +
                    "      \"manufacturerName\": \"vinodkumar\",\n" +
                    "      \"createdTime\": \"2022-03-28 18:24:50\"\n" +
                    "    },\n" +
                    "    {\n" +
                    "      \"manufacturerName\": \"vinod\",\n" +
                    "      \"createdTime\": \"2022-03-28 18:24:50\"\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}"
            val intent = Intent(this, ProductActivity::class.java)
            intent.putExtra("id", "")
            intent.putExtra("product", jsonString)
            intent.putExtra("userdetail", userDetailString)
            startActivity(intent)*/
            ScanBarcode()
        }
    }

    fun ScanBarcode() {
        barcodeLauncher.launch(ScanOptions())
    }

    private val barcodeLauncher = registerForActivityResult(
        ScanContract()
    ) { result: ScanIntentResult ->
        if (result.contents == null) {
            val originalIntent = result.originalIntent
            if (originalIntent == null) {
                Log.d("MainActivity", "Cancelled scan")
                Toast.makeText(this@MenuActivity, "Cancelled", Toast.LENGTH_LONG).show()
            } else if (originalIntent.hasExtra(Intents.Scan.MISSING_CAMERA_PERMISSION)) {
                Log.d(
                    "MainActivity",
                    "Cancelled scan due to missing camera permission"
                )
                Toast.makeText(
                    this@MenuActivity,
                    "Cancelled due to missing camera permission",
                    Toast.LENGTH_LONG
                ).show()
            }
        } else {
            Log.d("MenuActivity", "Scanned")
            try {
                val type = object : TypeToken<Product>() {}.type
                val product = Gson().fromJson<Product>(result.contents, type)
                getData(product)
            }catch (e: IllegalStateException){
                showFakeProductDialog()
            }catch (ex: TypeCastException){
                showFakeProductDialog()
            }catch (exception : Exception){
                showFakeProductDialog()
            }
            /*Toast.makeText(
                this@MenuActivity,
                "Scanned: " + result.contents,
                Toast.LENGTH_LONG
            ).show()*/
            /*AlertDialog.Builder(this)
                .setTitle("Product Details")
                .setCancelable(true)
                .setMessage(result.contents)
                .setPositiveButton("Ok") { dialog, which ->
                    dialog.dismiss()
                }
                //.setNegativeButton(R.string.no, null)
                .create()
                .show()*/
        }
    }

    fun getData(product: Product){
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
                    /*AlertDialog.Builder(this)
                        .setTitle("Product Details")
                        .setCancelable(true)
                        .setMessage(data)
                        .setPositiveButton("Ok") { dialog, which ->
                            dialog.dismiss()
                        }
                        //.setNegativeButton(R.string.no, null)
                        .create()
                        .show()*/

                    try {

                        val yourData: MutableMap<String, Any> = document.data
                        val jsonString = Gson().toJson(yourData)
                        Log.d("json document.id ", document.id)
                        Log.d("json data ", Gson().toJson(yourData))
                        Log.d("json String  ", jsonString)

                        /*val type = object : TypeToken<Product>() {}.type
                        val newProduct = Gson().fromJson<Product>(jsonString, type)*/
                        /*val productString = Gson().toJson(newProduct)
                        Log.d("json productString  ", jsonString)*/
                        val intent = Intent(this, ProductActivity::class.java)
                        intent.putExtra("id", document.id)
                        intent.putExtra("product", jsonString)
                        intent.putExtra("userdetail", userDetailString)
                        startActivity(intent)
                    }catch (e: Exception){
                        Log.d("json convert error", e.localizedMessage)
                    }
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

    fun GoToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}