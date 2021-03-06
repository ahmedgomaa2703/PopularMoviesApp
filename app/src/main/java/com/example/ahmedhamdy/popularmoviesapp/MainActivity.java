package com.example.ahmedhamdy.popularmoviesapp;


import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.example.ahmedhamdy.popularmoviesapp.database.MovieContract;

import org.parceler.Parcels;

import java.util.ArrayList;



public class MainActivity extends AppCompatActivity {
    private static final String MOVIE_KEY = "movie";
    private static final String CURRENT_SELECTION = "index";
    private static final String CURRENT_SORT = "sort";
    private static final Uri movieUri = MovieContract.MovieEntry.CONTENT_URI;

    int index = 1;
    private GridView gridView;
    private MoviesAdapter moviesAdapter;
    private ArrayList<MoviesDb> movies = new ArrayList<>();
    private Button refreshButton;
    private String sortedBY = "top";

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(CURRENT_SORT, sortedBY);
        index = gridView.getFirstVisiblePosition();
        outState.putInt(CURRENT_SELECTION, index);

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gridView = (GridView) findViewById(R.id.moviesgridview);

        if (savedInstanceState == null) {
            startApp(sortedBY);
        } else {
            sortedBY = savedInstanceState.getString(CURRENT_SORT);
            index = savedInstanceState.getInt(CURRENT_SELECTION);
            // Toast.makeText(getApplicationContext(),String.valueOf(index),Toast.LENGTH_SHORT).show();
            if (sortedBY.equals("fav")) {
                startFavorites();
            } else {
                initStart(sortedBY);

            }

        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.actionbar, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_popular:
                startApp("popular");
                sortedBY = "popular";
                Toast.makeText(getApplicationContext(), sortedBY, Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_toprated:
                startApp("top");
                sortedBY = "top";
                Toast.makeText(getApplicationContext(), sortedBY, Toast.LENGTH_SHORT).show();
                break;
            case R.id.favorites:
                startFavorites();
                sortedBY = "fav";


        }
        return super.onOptionsItemSelected(item);
    }

    private void startApp(String sortby) {


        moviesAdapter = new MoviesAdapter(this, movies);
        moviesAdapter.clear();
        final RequestQueue queue = Volley.newRequestQueue(this);
        gridView.setAdapter(moviesAdapter);


        TheMovieDbClient.getJsonString(queue, this, moviesAdapter, sortby, new TheMovieDbClient.movieListLoaded() {
            @Override
            public void movieListLoaded() {
                if (index != 1) {

                    gridView.setSelection(index);
                   // Toast.makeText(getApplicationContext(),String.valueOf(index),Toast.LENGTH_SHORT).show();
                }
            }
        });



        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                MoviesDb movie = moviesAdapter.getItem(position);
                String trialersRequest = TheMovieDbClient.getVideosRequestUrl(movie.getMovieId());

                String reviewsUrl = TheMovieDbClient.reviewsUrl(movie.getMovieId());

                TheMovieDbClient.getReviewsResponse(getApplicationContext(), queue, reviewsUrl);

                TheMovieDbClient.getTrailersKey(queue, getApplicationContext(), trialersRequest);

                Intent i = new Intent(MainActivity.this, MovieDetailsActivity.class);
                i.putExtra(MOVIE_KEY, Parcels.wrap(movie));
                //Toast.makeText(getApplicationContext(),movie.getTitle(),Toast.LENGTH_LONG).show();
                startActivity(i);
            }
        });

    }


    public void startFavorites() {



        // Done: get all movies form content provider
        movies.clear();
        getAllFavMovies(movies);
        moviesAdapter = new MoviesAdapter(this, movies);
        gridView.setAdapter(moviesAdapter);
        gridView.setSelection(index);


    }
    public void getAllFavMovies(ArrayList<MoviesDb> arrayList){


       Cursor cursor = getContentResolver().query(movieUri,null,null,null,null);
      try {
          if (cursor.moveToFirst())
          {
              do {
                  MoviesDb movie  = new MoviesDb();
                  movie.title = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.KEY_TITLE));
                  movie.voteAverage = cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry.KEY_VOTE_AVERAGE));
                  movie.posterPath = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.KEY_POSTER_PATH));
                  movie.overView = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.KEY_OVERVIEW));
                  movie.realeseDate = cursor.getString(cursor.getColumnIndex(MovieContract.MovieEntry.KEY_RELEASE_DATE));
                  movie.movieId =cursor.getInt(cursor.getColumnIndex(MovieContract.MovieEntry.KEY_MOVIE_ID));

                  arrayList.add(movie);


              }
              while (cursor.moveToNext());
          }
      }
      catch (Exception e){
          Toast.makeText(getApplicationContext(),e.toString(),Toast.LENGTH_SHORT).show();
      }
      finally {
          if (cursor != null && !cursor.isClosed())
              cursor.close();
      }

    }


    private Boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }


    public void initStart(final String sort) {
        if (!isNetworkAvailable()) {
            setContentView(R.layout.offline_layout);
            refreshButton = (Button) findViewById(R.id.refreshbutton);
            refreshButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isNetworkAvailable()) {
                        if (!TextUtils.isEmpty(sort))
                            startApp(sort);
                        else
                            startApp(sort);
                    } else {

                    }
                }
            });
        } else {
            if (!TextUtils.isEmpty(sort))
                startApp(sort);
            else
                startApp("popular");
        }

    }

}
