package layout.RecyclerClick_Listener;

import android.view.View;

/**
 * Created by Darshan on 27-03-2017.
 */
public interface RecyclerClick_Listener {

    /**
     * Interface for Recycler View Click listener
     **/

    void onClick(View view, int position);

    void onLongClick(View view, int position);
}