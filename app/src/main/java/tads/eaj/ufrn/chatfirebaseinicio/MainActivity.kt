package tads.eaj.ufrn.chatfirebaseinicio

/**
 * Copyright Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
Modified by Taniro 15/11/2019 to add support to kotlin
 */
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import tads.eaj.ufrn.tadschatinicio.FriendlyMessage
import tads.eaj.ufrn.tadschatinicio.MessageAdapter
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_main.*
import com.google.firebase.database.FirebaseDatabase
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import java.util.*
import java.util.Arrays.asList
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    val ANONYMOUS = "anonymous"
    val DEFAULT_MSG_LENGTH_LIMIT = 1000
    private val CODIGO_LOGAR = 55

    private val friendlyMessages = ArrayList<FriendlyMessage>()
    private val mMessageAdapter by lazy {
        MessageAdapter(this, R.layout.item_message, friendlyMessages)
    }

    private lateinit var mUsername: String

    private lateinit var mFirebaseDatabase: FirebaseDatabase
    private lateinit var mMessagesDatabaseReference: DatabaseReference

    private var mChildEventListener: ChildEventListener? = null

    private lateinit var mFirebaseAuth: FirebaseAuth

    private lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mUsername = ANONYMOUS

        mFirebaseDatabase = FirebaseDatabase.getInstance()
        mMessagesDatabaseReference = mFirebaseDatabase.reference.child("/messages")

        messageListView.adapter = mMessageAdapter

        mFirebaseDatabase = FirebaseDatabase.getInstance()

        mFirebaseAuth = FirebaseAuth.getInstance()
        mFirebaseAuth.addAuthStateListener(mAuthStateListener)


        // ImagePickerButton shows an image picker to upload a image for a message
        photoPickerButton.setOnClickListener {
            // TODO: Fire an intent to show an image picker
        }

        messageEditText.filters = arrayOf(InputFilter.LengthFilter(DEFAULT_MSG_LENGTH_LIMIT))
        messageEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(texto: CharSequence?, p1: Int, p2: Int, p3: Int) {
                sendButton.isEnabled = texto.toString().trim().isNotEmpty()
            }
        })

        sendButton.setOnClickListener {

            var friendlyMessage = FriendlyMessage(messageEditText.text.toString(), mUsername, null)
            mMessagesDatabaseReference.push().setValue(friendlyMessage)

            // Clear input box
            messageEditText.setText("");
        }
        /*
        mChildEventListener = object : ChildEventListener {
            override fun onChildRemoved(p0: DataSnapshot) {}
            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {

                var friendlyMessage = dataSnapshot.getValue(FriendlyMessage::class.java)
                mMessageAdapter.add(friendlyMessage)
            }
        }
        mChildEventListener?.let { mMessagesDatabaseReference.addChildEventListener(it) }
        */

        mAuthStateListener =
            FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user != null) {
                //logado
                //Toast.makeText(MainActivity.this, "Logado", Toast.LENGTH_SHORT).show();
                onSignInInitialize(user.displayName)
            } else {
                //não-logado
                onSignOutCleanUp()
                //chama o fluxo de login
                startActivityForResult(
                    AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setAvailableProviders(
                            asList(
                                AuthUI.IdpConfig.GoogleBuilder().build(),
                                AuthUI.IdpConfig.EmailBuilder().build()
                            )
                        )
                        .build(),
                    CODIGO_LOGAR
                )
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    private fun onSignInInitialize(username: String?) {
        mUsername = username ?: ANONYMOUS
        attachDatabaseReadListener()
    }

    private fun onSignOutCleanUp() {
        mUsername = ANONYMOUS
        mMessageAdapter.clear()
        detachDatabaseReadListener()
    }

    private fun attachDatabaseReadListener() {
        if (mChildEventListener == null) {
            mChildEventListener = object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                    val friendlyMessage = dataSnapshot.getValue(FriendlyMessage::class.java)
                    mMessageAdapter.add(friendlyMessage)
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {}
                override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
                override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {}
                override fun onCancelled(databaseError: DatabaseError) {}
            }
            mMessagesDatabaseReference.addChildEventListener(mChildEventListener!!)
        }
    }

    private fun detachDatabaseReadListener() {
        if (mChildEventListener != null) {
            mMessagesDatabaseReference.removeEventListener(mChildEventListener!!)
            mChildEventListener = null
        }
    }

    override fun onResume() {
        super.onResume()
        mFirebaseAuth.addAuthStateListener(mAuthStateListener)
    }

    override fun onPause() {
        super.onPause()
        mFirebaseAuth.removeAuthStateListener(mAuthStateListener)
        detachDatabaseReadListener()
        mMessageAdapter.clear()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CODIGO_LOGAR) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Bem-vindo", Toast.LENGTH_SHORT).show()
            } else if (resultCode == RESULT_CANCELED) {
                finish()
            }
        }
    }
}
