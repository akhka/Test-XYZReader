package com.gcs.testxyzreader.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityOptionsCompat;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.gcs.testxyzreader.R;
import com.gcs.testxyzreader.data.ArticleLoader;
import com.gcs.testxyzreader.ui.ArticleDetailActivity;
import com.gcs.testxyzreader.ui.DynamicHeightNetworkImageView;
import com.gcs.testxyzreader.ui.ImageLoaderHelper;
import com.gcs.testxyzreader.utils.Utils;
import com.squareup.picasso.Picasso;

public class ArticleListAdapter extends RecyclerView.Adapter<ArticleListAdapter.ViewHolder> {

    private Cursor cursor;
    private Context context;
    private int resource;

    public ArticleListAdapter(Cursor cursor, Context context, int resource){
        this.context = context;
        this.cursor = cursor;
        this.resource = resource;
    }

    @Override
    public long getItemId(int position) {
        cursor.moveToPosition(position);
        return cursor.getLong(ArticleLoader.Query._ID);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(resource, parent, false);
        final ViewHolder vh = new ViewHolder(view);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = vh.getAdapterPosition();
                ActivityOptionsCompat activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation((Activity) context, vh.thumbnailView, vh.thumbnailView.getTransitionName());
                Intent intent = new Intent(context, ArticleDetailActivity.class);
                intent.putExtra(Utils.CURRENT_ARTICLE_POSITION, position);
                intent.putExtra(Utils.MUTED_COLOR_VALUE, vh.mutedColor);
                intent.putExtra(Utils.CURRENT_ARTICLE_TRANSITION_NAME, vh.thumbnailView.getTransitionName());
                context.startActivity(intent, activityOptionsCompat.toBundle());
            }
        });

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        cursor.moveToPosition(position);

        // Setting transition name and tag for thumbnailView
        holder.thumbnailView.setTransitionName(context.getString(R.string.book_image) + position);
        holder.thumbnailView.setTag(context.getString(R.string.book_image) + position);

        // Getting data from DB
        String title = cursor.getString(ArticleLoader.Query.TITLE);
        String subtitle = DateUtils.getRelativeTimeSpanString(
               cursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                DateUtils.FORMAT_ABBREV_ALL).toString();
        String author = cursor.getString(ArticleLoader.Query.AUTHOR);
        final String imageUrl = cursor.getString(ArticleLoader.Query.THUMB_URL);

        try{
            holder.bitmap = new AsyncTask<Void, Void, Bitmap>(){

                @Override
                protected Bitmap doInBackground(Void... voids) {
                    try{
                        return Picasso.get().load(imageUrl).get();
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                    return null;
                }
            }.execute().get();
        } catch (Exception e){
            e.printStackTrace();
        }

        holder.titleView.setText(title);
        holder.subtitleView.setText(subtitle);
        holder.authorView.setText(author);
        holder.thumbnailView.setAspectRatio(cursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));

        // Getting image's dark muted color
        ImageLoader loader = ImageLoaderHelper.getInstance(context).getImageLoader();
        holder.thumbnailView.setImageUrl(imageUrl, loader);
        loader.get(imageUrl, new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
                Bitmap bitmap = imageContainer.getBitmap();
                if(bitmap != null) {
                    Palette p = Palette.from(bitmap).generate();
                    int mMutedColor = p.getDarkVibrantColor(Utils.DEFAULT_COLOR);
                    holder.mutedColor = mMutedColor;
                    holder.itemView.setBackgroundColor(mMutedColor);
                    holder.thumbnailView.setBackgroundColor(mMutedColor);
                }
            }

            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private DynamicHeightNetworkImageView thumbnailView;
        private TextView titleView, subtitleView, authorView;
        private CardView cardView;
        Bitmap bitmap = null;
        int mutedColor;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailView = itemView.findViewById(R.id.thumbnail);
            titleView = itemView.findViewById(R.id.article_title);
            subtitleView = itemView.findViewById(R.id.article_subtitle);
            authorView = itemView.findViewById(R.id.article_author);
            cardView = itemView.findViewById(R.id.cardView);
        }
    }

}
