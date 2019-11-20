package tads.eaj.ufrn.tadschatinicio


import android.view.View
import android.widget.ImageView
import android.widget.TextView
import tads.eaj.ufrn.chatfirebaseinicio.R

class MensagemViewHolder  (v: View){

    var photoImageView: ImageView
    var messageTextView:TextView
    var authorTextView:TextView

    init {
        photoImageView = v.findViewById(R.id.photoImageView)
        messageTextView = v.findViewById(R.id.messageTextView)
        authorTextView = v.findViewById(R.id.nameTextView)
    }


}
