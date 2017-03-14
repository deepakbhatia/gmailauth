package com.optimalcities.gmailauthenticationapi.Binding;

/**
 * Created by obelix on 13/03/2017.
 */

import android.databinding.BindingAdapter;
import android.widget.ImageView;

import com.optimalcities.gmailauthenticationapi.R;
import com.squareup.picasso.Picasso;


/**
 * Created by troy379 on 15.03.16.
 */
public final class BindingAdapters {
    private BindingAdapters() { throw new AssertionError(); }

    @BindingAdapter("bind:imageUrl")
    public static void loadImage(ImageView view, String url) {
        Picasso.with(view.getContext())
                .load(url)
                .placeholder(R.drawable.ic_person)
                .into(view);
    }


}
