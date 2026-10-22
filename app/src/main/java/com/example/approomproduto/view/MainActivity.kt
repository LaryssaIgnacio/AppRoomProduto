package com.example.approomproduto.view

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.approomproduto.dao.ProductDao
import com.example.approomproduto.databinding.ActivityMainBinding
import com.example.approomproduto.db.AppDatabase
import com.example.approomproduto.model.Product
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: AppDatabase
    private lateinit var productDao: ProductDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializa o ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializa o banco de dados Room
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "database-name"
        )
            .fallbackToDestructiveMigration()
            .build()

        productDao = db.productDao()

        // Inserir produto
        binding.btnInserirProduto.setOnClickListener {
            val name = binding.editProductName.text.toString().trim()
            val priceText = binding.editProductPrice.text.toString()
            val description = binding.editProductDescription.text.toString()
            // A8: lê a quantidade (se ficar vazio, salva 0)
            val quantity = binding.editProductQuantity.text.toString().toIntOrNull() ?: 0

            // Nome obrigatório
            if (name.isEmpty()) {
                binding.editProductName.error = "Informe o nome do produto"
                return@setOnClickListener
            }

            // A2: não permite preço vazio, zero ou negativo
            val price = priceText.toDoubleOrNull()
            if (price == null || price <= 0) {
                binding.editProductPrice.error = "Informe um preço válido"
                return@setOnClickListener
            }

            val product = Product(
                name = name,
                price = price,
                description = description,
                quantity = quantity
            )

            lifecycleScope.launch {
                productDao.insertAll(product)
                Log.i("TAG", "Produto inserido: $name - R$$price - Qtd: $quantity")
                Toast.makeText(this@MainActivity, "Produto inserido!", Toast.LENGTH_SHORT).show()

                // A1: limpa os campos e volta o cursor para o Nome
                binding.editProductName.text.clear()
                binding.editProductPrice.text.clear()
                binding.editProductDescription.text.clear()
                binding.editProductQuantity.text.clear()
                binding.editProductName.requestFocus()
            }
        }

        // Listar produtos
        binding.btnListarProdutos.setOnClickListener {
            lifecycleScope.launch {
                val products = productDao.getAll()
                mostrarProdutos(products)

                // Continua mostrando no Logcat também
                products.forEach {
                    Log.i("TAG", "Produto = ${it.name}, Preço = R$${it.price}, Descrição = ${it.description}, Quantidade = ${it.quantity}")
                }
            }
        }

        // A5: buscar por nome
        binding.btnBuscar.setOnClickListener {
            val texto = binding.editBusca.text.toString().trim()

            lifecycleScope.launch {
                // "%texto%" encontra nomes que CONTÊM o texto digitado
                val products = productDao.buscarPorNome("%$texto%")
                mostrarProdutos(products)
            }
        }

        // A6: excluir todos os produtos
        binding.btnExcluirTodos.setOnClickListener {
            lifecycleScope.launch {
                productDao.deleteAll()
                mostrarProdutos(emptyList())
                Toast.makeText(this@MainActivity, "Todos os produtos foram excluídos", Toast.LENGTH_SHORT).show()
            }
        }

        // A7: atualizar o preço de um produto pelo id
        binding.btnAtualizarPreco.setOnClickListener {
            val id = binding.editProductId.text.toString().toIntOrNull()
            val novoPreco = binding.editNovoPreco.text.toString().toDoubleOrNull()

            if (id == null) {
                binding.editProductId.error = "Informe o ID"
                return@setOnClickListener
            }
            if (novoPreco == null || novoPreco <= 0) {
                binding.editNovoPreco.error = "Informe um preço válido"
                return@setOnClickListener
            }

            lifecycleScope.launch {
                val produto = productDao.getById(id)

                if (produto == null) {
                    Toast.makeText(this@MainActivity, "Produto não encontrado", Toast.LENGTH_SHORT).show()
                } else {
                    // copy() cria uma cópia do produto só com o preço alterado
                    productDao.update(produto.copy(price = novoPreco))
                    Toast.makeText(this@MainActivity, "Preço atualizado!", Toast.LENGTH_SHORT).show()

                    binding.editProductId.text.clear()
                    binding.editNovoPreco.text.clear()
                    mostrarProdutos(productDao.getAll())
                }
            }
        }
    }

    // A3 + A4: mostra a lista de produtos e o total do estoque
    private fun mostrarProdutos(products: List<Product>) {
        if (products.isEmpty()) {
            binding.txtTotal.text = ""
            binding.txtListaProdutos.text = "Nenhum produto encontrado."
            return
        }

        // A4: quantidade de produtos e soma dos preços
        val quantidade = products.size
        val soma = products.sumOf { it.price }
        binding.txtTotal.text =
            "Produtos cadastrados: $quantidade | Soma dos preços: R$ ${"%.2f".format(soma)}"

        // A3: lista na tela (A8: agora mostra a quantidade também)
        binding.txtListaProdutos.text = products.joinToString("\n\n") {
            "ID ${it.id} - ${it.name}\n" +
                    "Preço: R$ ${it.price}\n" +
                    "Descrição: ${it.description}\n" +
                    "Quantidade: ${it.quantity}"
        }
    }
}