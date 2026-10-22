package com.example.approomproduto.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.approomproduto.model.Product

@Dao
interface ProductDao {
    // suspend: o Room não permite consultas na main thread
    @Query("SELECT * FROM product")
    suspend fun getAll(): List<Product>

    @Insert
    suspend fun insertAll(vararg products: Product)

    // A5: busca produtos pelo nome
    @Query("SELECT * FROM product WHERE name LIKE :nome")
    suspend fun buscarPorNome(nome: String): List<Product>

    // A6: exclui todos os produtos
    @Query("DELETE FROM product")
    suspend fun deleteAll()

    // A7: busca um produto pelo id
    @Query("SELECT * FROM product WHERE id = :id")
    suspend fun getById(id: Int): Product?

    // A7: atualiza um produto já cadastrado
    @Update
    suspend fun update(product: Product)
}