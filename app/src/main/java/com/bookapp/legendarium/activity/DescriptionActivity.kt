package com.bookapp.legendarium.activity

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.room.Room
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.bookapp.legendarium.R
import com.bookapp.legendarium.database.BookDatabase
import com.bookapp.legendarium.database.BookEntity
import com.bookapp.legendarium.util.ConnectionManager
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_description.*


class DescriptionActivity : AppCompatActivity() {
    var bookId: String? = "100" // Задаём на всякий случай

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Ставим загрузуку в самом начале
        setContentView(R.layout.activity_description)
        setSupportActionBar(toolbar)
        supportActionBar?.title = ""

        back_button.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // Получаем параметр интента
        if (intent != null) {
            bookId = intent.getStringExtra("book_id")
        } else {
            finish()
            // Выводим ошибку, если нет параметра
            Toast.makeText(
                this@DescriptionActivity,
                "Some unexpected error occurred!",
                Toast.LENGTH_SHORT
            ).show()
            Log.d("DescriptionActivity", "Ошибка Intent")
        }

        val queue = Volley.newRequestQueue(this@DescriptionActivity)
        // Генерируем адрес в зависимости от id книги
        val url = "https://run.mocky.io/v3/$bookId"
        Log.d("DescriptionActivity", url)

        // Проверяем соединение с интернетом
        if (ConnectionManager().checkConnectivity(this@DescriptionActivity)) {
            val jsonRequest =
                @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
                object : JsonObjectRequest(Request.Method.GET, url, null, Response.Listener {

                    try {

                        val success = it.getBoolean("success")
                        if (success) {
                            Log.d("DescriptionActivity", "Запрос на получение книги прошёл успешно")
                            val bookJsonObject = it.getJSONObject("book_data")
                            progressLayout.visibility = View.GONE

                            // Расставляем значения в activity
                            val bookImageUrl = bookJsonObject.getString("image")
                            // Если не загрузится изображение - будет стандартное
                            Picasso.get().load(bookJsonObject.getString("image"))
                                .error(R.drawable.default_book_cover).into(imgBookImage)
                            val wideImage = bookJsonObject.getString("wide_image")
                            Picasso.get().load(bookJsonObject.getString("wide_image"))
                                .error(R.drawable.default_book_cover).into(imgBookImage)
                            txtBookName.text = bookJsonObject.getString("name")
                            txtBookAuthor.text = bookJsonObject.getString("author")
                            txtBookDesc.text = bookJsonObject.getString("description")
                            val book = bookJsonObject.getString("book")
                            Log.d("DescriptionActivity", "Activity заполнена данными")

                            // Создаём объект книги в БД
                            val bookEntity = BookEntity(
                                bookId.toString(),
                                txtBookName.text.toString(),
                                txtBookAuthor.text.toString(),
                                txtBookDesc.text.toString(),
                                bookImageUrl,
                                wideImage,
                                book
                            )
                            Log.d("DescriptionActivity", "Создан объект БД")

                            val checkFav = DBAsyncTask(applicationContext, bookEntity, 1).execute()
                            val isFav = checkFav.get()

                            if (isFav) {
                                Log.d(
                                    "DescriptionActivity",
                                    "Книга была добавлена в избранное ранее"
                                )
                                add_to_favourites.setBackgroundResource(R.drawable.ic_star)
                            } else {
                                Log.d("DescriptionActivity", "Книга не была добавлена в избранное")
                                add_to_favourites.setBackgroundResource(R.drawable.ic_empty_star)
                            }

                            start_reading_button.setOnClickListener {
                                val uri = Uri.parse(book)
                                intent = Intent(Intent.ACTION_VIEW, uri)
                                startActivity(intent)
                            }

                            add_to_favourites.setOnClickListener {

                                if (!DBAsyncTask(
                                        applicationContext,
                                        bookEntity,
                                        1
                                    ).execute().get()
                                ) {

                                    val async =
                                        DBAsyncTask(applicationContext, bookEntity, 2).execute()
                                    val result = async.get()
                                    if (result) {
                                        Toast.makeText(
                                            this@DescriptionActivity,
                                            "Book added to favourites",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Log.d("DescriptionActivity", "Книга добавлена в избранное")
                                        add_to_favourites.setBackgroundResource(R.drawable.ic_star)
                                    } else {
                                        Log.d("DescriptionActivity", "Книга добавлена в избранное")
                                        Toast.makeText(
                                            this@DescriptionActivity,
                                            "Some error occurred!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {

                                    val async =
                                        DBAsyncTask(applicationContext, bookEntity, 3).execute()
                                    val result = async.get()

                                    if (result) {
                                        Toast.makeText(
                                            this@DescriptionActivity,
                                            "Book removed from favourites",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        Log.d("DescriptionActivity", "Книга удалена из избранного")
                                        add_to_favourites.setBackgroundResource(R.drawable.ic_empty_star)
                                    } else {
                                        Toast.makeText(
                                            this@DescriptionActivity,
                                            "Some error occurred!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                }
                            }

                        } else {
                            Toast.makeText(
                                this@DescriptionActivity,
                                "Some Error Occurred!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } catch (e: Exception) {
                        Toast.makeText(
                            this@DescriptionActivity,
                            "Some error occurred!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }, Response.ErrorListener {

                    Toast.makeText(this@DescriptionActivity, "Volley Error $it", Toast.LENGTH_SHORT)
                        .show()

                }) {}
            queue.add(jsonRequest)
        } else {
            // Выводим ошибку, если нет соединения
            val dialog = AlertDialog.Builder(this@DescriptionActivity)
            dialog.setTitle("Error")
            dialog.setMessage("Internet Connection is not Found")
            // Если пользователь нажал "Открыть настройки" - направляем в настройки
            dialog.setPositiveButton("Open Settings") { text, listener ->
                val settingsIntent = Intent(Settings.ACTION_WIRELESS_SETTINGS)
                startActivity(settingsIntent)
                finish()
            }

            // Если пользователь нажал "Выход"
            dialog.setNegativeButton("Exit") { text, listener ->
                ActivityCompat.finishAffinity(this@DescriptionActivity)
            }
            dialog.create()
            dialog.show()
        }

    }


    class DBAsyncTask(
        val context: Context,
        private val bookEntity: BookEntity,
        private val mode: Int
    ) :
        AsyncTask<Void, Void, Boolean>() {

        /*
            1 - Проверяем, если ли книга в избранном
            2 - Сохраняем книгу в избранное
            3 - Удаляем книгу из избранного
        */

        val db = Room.databaseBuilder(context, BookDatabase::class.java, "books-db").build()

        @Deprecated("Later")
        override fun doInBackground(vararg p0: Void?): Boolean {

            when (mode) {
                1 -> {
                    // 1
                    val book: BookEntity? = db.bookDao().getBookById(bookEntity.book_id)
                    db.close()
                    return book != null
                }
                2 -> {
                    // 2
                    db.bookDao().insertBook(bookEntity)
                    db.close()
                    return true
                }
                3 -> {
                    // 3
                    db.bookDao().deleteBook(bookEntity)
                    db.close()
                    return true
                }
            }
            return false
        }

    }
}
