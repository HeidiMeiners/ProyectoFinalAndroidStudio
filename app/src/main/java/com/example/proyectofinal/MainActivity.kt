package com.example.proyectofinal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference

    private val barcodeLauncher = registerForActivityResult(
        ScanContract()) { result: ScanIntentResult? ->

        if (result?.contents == null) {

            Toast.makeText(
                this,
                "Cancelado",
                Toast.LENGTH_LONG
            ).show()

        } else {
            val codigoQR = result.contents
            buscarQR(codigoQR)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        database = FirebaseDatabase.getInstance().reference
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        val qr = findViewById<Button>(R.id.qr)
        qr.setOnClickListener {
            leerQR()
        }

        contador(0)

        val logout = findViewById<FloatingActionButton>(R.id.logout)
        logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(
                Intent(this, Login::class.java)
            )
            finish()
        }
    }

    fun leerQR() {
        val options = ScanOptions()

        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        options.setPrompt("Escanea un QR")
        options.setCameraId(0)
        options.setBeepEnabled(true)
        options.setBarcodeImageEnabled(true)

        barcodeLauncher.launch(options)
    }

    fun buscarQR(codigoQR: String) {

        database.child("claves")
            .child(codigoQR)
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()) {

                        val status = snapshot.child("status")
                            .getValue(String::class.java)

                        if (status == "generado") {

                            database.child("claves")
                                .child(codigoQR)
                                .child("status")
                                .setValue("utilizado")

                            database.child("claves")
                                .child(codigoQR)
                                .child("fechaUso")
                                .setValue(System.currentTimeMillis())

                            Toast.makeText(
                                this@MainActivity,
                                "Buen viaje",
                                Toast.LENGTH_LONG
                            ).show()

                            findViewById<TextView>(R.id.mensaje).setText("QR leido correctamente, buen viaje");

                            contador(1);
                        }

                        else if (status == "utilizado") {

                            Toast.makeText(
                                this@MainActivity,
                                "QR utilizado, genere un nuevo QR para ingresar",
                                Toast.LENGTH_LONG
                            ).show()
                        }

                    } else {

                        Toast.makeText(
                            this@MainActivity,
                            "QR no encontrado",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                    Toast.makeText(
                        this@MainActivity,
                        "Error de Firebase",
                        Toast.LENGTH_LONG
                    ).show()
                }
            })
    }

    fun contador(flag: Int) {

        if (flag==1) {
            val contadorRef =
                database.child("contadorQR").child("total")

            contadorRef.get().addOnSuccessListener { snapshot ->

                val totalActual =
                    snapshot.getValue(Int::class.java) ?: 0

                contadorRef.setValue(totalActual + 1)
            }
        }
        else{
            val contadorRef =
                database.child("contadorQR").child("total")

            contadorRef.get().addOnSuccessListener { snapshot ->

                val totalActual =
                    snapshot.getValue(Int::class.java) ?: 0

                findViewById<TextView>(R.id.numero).text = "$totalActual"
            }
        }
    }
}