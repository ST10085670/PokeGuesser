package vcmsa.projects.pokeguesser

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale
import androidx.core.graphics.toColorInt

class MainActivity : AppCompatActivity() {
    
    private lateinit var imgPokemon: ImageView
    private lateinit var edtPokemonName: EditText
    private lateinit var txtOutput: TextView
    private lateinit var btnSubmit: Button
    private lateinit var retryLayout: LinearLayout
    private lateinit var txtRetryResult: TextView
    private lateinit var imgRetryPokemon: ImageView
    private lateinit var btnRetry: Button
    private var currentSpriteUrl: String = ""
    private var lives = 3
    
    
    private var currentPokemonName: String = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        imgPokemon = findViewById(R.id.imgPokemon)
        edtPokemonName = findViewById(R.id.edtPokemonName)
        txtOutput = findViewById(R.id.txtOutput)
        btnSubmit = findViewById(R.id.btnSubmit)
        retryLayout = findViewById(R.id.retryLayout)
        txtRetryResult = findViewById(R.id.txtResult)
        imgRetryPokemon = findViewById(R.id.imgPokemonResult)
        btnRetry = findViewById(R.id.btnRetry)
        
        btnRetry.setOnClickListener {
            retryLayout.visibility = View.GONE
            fetchRandomPokemon()
            lives = 3
            btnSubmit.setBackgroundColor("#F44336".toColorInt()) // Reset to red
            
        }
        
        fetchRandomPokemon()
        
        btnSubmit.setOnClickListener {
            val userGuess = edtPokemonName.text.toString().trim().lowercase(Locale.ROOT)
            val actualName = currentPokemonName.lowercase(Locale.ROOT)
            
            if (userGuess == actualName) {
                txtOutput.text = "Correct! It's ${currentPokemonName.capitalize()}!"
                txtOutput.setTextColor(Color.parseColor("#4CAF50"))
                
                
                // Show retry screen (win case)
                txtRetryResult.text = "Correct! It was ${currentPokemonName.capitalize()}!"
                fetchRandomPokemon()
                
                
            } else {
                lives--
                
                when (lives) {
                    2 -> btnSubmit.setBackgroundColor(Color.parseColor("#FF9800")) // Orange
                    1 -> btnSubmit.setBackgroundColor(Color.parseColor("#FFEB3B")) // Yellow
                    0 -> {
                        txtOutput.text = "Wrong! You lost!"
                        txtOutput.setTextColor(Color.RED)
                        
                        txtRetryResult.text = "Wrong! It was ${currentPokemonName.capitalize()}!"
                        Picasso.get().load(currentSpriteUrl).into(imgRetryPokemon)
                        retryLayout.visibility = View.VISIBLE
                    }
                }
                
                if (lives > 0) {
                    txtOutput.text = "Wrong! Try again."
                    txtOutput.setTextColor(Color.parseColor("#F44336"))
                }
            }
        }
        
        
    }
    
    private fun fetchRandomPokemon() {
        val randomId = (1..151).random() // 1 to 151 for Gen 1 Pokémon
        val url = "https://pokeapi.co/api/v2/pokemon/$randomId"
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("MainActivity", "Fetching random Pokémon with ID $randomId")
                val client = OkHttpClient()
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                val json = response.body?.string() ?: return@launch
                
                val jsonObject = JSONObject(json)
                currentPokemonName = jsonObject.getString("name")
                val spriteUrl = jsonObject
                    .getJSONObject("sprites")
                    .getString("front_default")
                currentSpriteUrl = jsonObject
                    .getJSONObject("sprites")
                    .getString("front_default")
                
                
                Log.d("MainActivity", "Fetched Pokémon $currentPokemonName with sprite URL $spriteUrl")
                
                
                withContext(Dispatchers.Main) {
                    Log.d("MainActivity", "Loading sprite URL $spriteUrl")
                    Picasso.get().load(spriteUrl).into(imgPokemon)
                    edtPokemonName.setText("")
                    txtOutput.text = ""
                }
                
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to load Pokémon", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Failed to load Pokémon", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}