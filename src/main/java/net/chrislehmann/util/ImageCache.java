package net.chrislehmann.util;

import java.net.URL;

import android.widget.ImageView;

interface ImageCache
{
   public void put(String name, URL image);

   public void clear();

   public void load(String name, ImageView imageView);

   public boolean has(String name);
}