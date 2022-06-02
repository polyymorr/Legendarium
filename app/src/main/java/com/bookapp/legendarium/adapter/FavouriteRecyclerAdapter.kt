package com.bookapp.legendarium.adapter

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bookapp.legendarium.R
import com.bookapp.legendarium.activity.DescriptionActivity
import com.bookapp.legendarium.database.BookEntity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.recycler_favourite_single_row.*
import kotlinx.android.synthetic.main.recycler_favourite_single_row.view.*

class FavouriteRecyclerAdapter(val context: Context, val bookList: List<BookEntity>) :
    RecyclerView.Adapter<FavouriteRecyclerAdapter.FavouriteViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_favourite_single_row, parent, false)

        return FavouriteViewHolder(view)
    }

    override fun getItemCount(): Int {
        return bookList.size
    }

    override fun onBindViewHolder(holder: FavouriteViewHolder, position: Int) {

        val book = bookList[position]

        holder.txtBookName.text = book.bookName
        Picasso.get().load(book.bookWideImage).error(R.drawable.default_book_cover).into(holder.imgBookImage)
        holder.startReadingButton.setOnClickListener {
            val url = Uri.parse(book.book)
            val intent = Intent(Intent.ACTION_VIEW, url)
            context.startActivity(intent)
        }

        holder.imgBookImage.setOnClickListener {
            val intent = Intent(context, DescriptionActivity::class.java)
            intent.putExtra("book_id", book.book_id)
            context.startActivity(intent)
        }
    }

    class FavouriteViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtBookName: TextView = view.findViewById(R.id.txtFavBookTitle)
        val imgBookImage: ImageView = view.findViewById(R.id.imgFavBookImage)
        val startReadingButton: Button = view.findViewById(R.id.start_reading_button)
        val llContent: LinearLayout = view.findViewById(R.id.llFavContent)
    }
}

