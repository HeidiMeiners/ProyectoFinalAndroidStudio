package com.example.proyectofinal

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.google.firebase.auth.FirebaseAuth

class Login : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser != null) {

            startActivity(
                Intent(this, MainActivity::class.java)
            )

            finish()
        }

        val correo = findViewById<EditText>(R.id.emailEdit)
        val password = findViewById<EditText>(R.id.contrasenaEdit)
        val login = findViewById<Button>(R.id.loginBtn)

        login.setOnClickListener {

            val email = correo.text.toString()
            val pass = password.text.toString()

            if(email.isEmpty() || pass.isEmpty()){

                Toast.makeText(
                    this,
                    "Completa todos los campos",
                    Toast.LENGTH_SHORT
                ).show()

            }else{

                iniciarSesion(email, pass)
            }
        }
    }

    private fun iniciarSesion(email: String, pass: String){

        auth.signInWithEmailAndPassword(email, pass)
            .addOnCompleteListener(this){ task ->

                if(task.isSuccessful){

                    Toast.makeText(
                        this,
                        "Login correcto",
                        Toast.LENGTH_SHORT
                    ).show()

                    startActivity(
                        Intent(this, MainActivity::class.java)
                    )

                    finish()

                }else{

                    Toast.makeText(
                        this,
                        "Correo o contraseña incorrectos",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
}