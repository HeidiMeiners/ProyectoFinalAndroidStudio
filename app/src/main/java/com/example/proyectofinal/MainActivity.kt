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

                    // SI EXISTE EL QR
                    if (snapshot.exists()) {

                        val status = snapshot.child("status")
                            .getValue(String::class.java)

                        // SI ESTÁ GENERADO
                        if (status == "generado") {

                            // CAMBIAR ESTATUS
                            database.child("claves")
                                .child(codigoQR)
                                .child("status")
                                .setValue("utilizado")

                            // ACTUALIZAR FECHA DE USO
                            database.child("claves")
                                .child(codigoQR)
                                .child("fechaUso")
                                .setValue(System.currentTimeMillis())

                            Toast.makeText(
                                this@MainActivity,
                                "Buen viaje",
                                Toast.LENGTH_LONG
                            ).show()

                            findViewById<TextView>(R.id.mensaje).setText("QR leiod correctamente, buen viaje");
                        }

                        // SI YA FUE UTILIZADO
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
}