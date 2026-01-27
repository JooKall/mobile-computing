package com.example.hw4sensorsandnotifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil3.compose.rememberAsyncImagePainter
import com.example.hw4sensorsandnotifications.ui.theme.HW4SensorsAndNotificationsTheme
import kotlinx.serialization.Serializable
import java.io.File

/**
 * SampleData for Jetpack Compose Tutorial
 */
object SampleData {
    // Sample conversation data
    val conversationSample = listOf(
        Message(
            "Joonas",
            "Test...Test...Test..."
        ),
        Message(
            "Joonas",
            """List of Android versions:
            |Android KitKat (API 19)
            |Android Lollipop (API 21)
            |Android Marshmallow (API 23)
            |Android Nougat (API 24)
            |Android Oreo (API 26)
            |Android Pie (API 28)
            |Android 10 (API 29)
            |Android 11 (API 30)
            |Android 12 (API 31)""".trim()
        ),
        Message(
            "Joonas",
            """I think Kotlin is my favorite programming language.
            |It's so much fun!""".trim()
        ),
        Message(
            "Joonas",
            "Searching for alternatives to XML layouts..."
        ),
        Message(
            "Joonas",
            """Hey, take a look at Jetpack Compose, it's great!
            |It's the Android's modern toolkit for building native UI.
            |It simplifies and accelerates UI development on Android.
            |Less code, powerful tools, and intuitive Kotlin APIs :)""".trim()
        ),
        Message(
            "Joonas",
            "It's available from API 21+ :)"
        ),
        Message(
            "Joonas",
            "Writing Kotlin for UI seems so natural, Compose where have you been all my life?"
        ),
        Message(
            "Joonas",
            "Android Studio next version's name is Arctic Fox"
        ),
        Message(
            "Joonas",
            "Android Studio Arctic Fox tooling for Compose is top notch ^_^"
        ),
        Message(
            "Joonas",
            "I didn't know you can now run the emulator directly from Android Studio"
        ),
        Message(
            "Joonas",
            "Compose Previews are great to check quickly how a composable layout looks like"
        ),
        Message(
            "Joonas",
            "Previews are also interactive after enabling the experimental setting"
        ),
        Message(
            "Joonas",
            "Have you tried writing build.gradle with KTS?"
        ),
        Message(
            "Joonas",
            "TEST!"
        ),
        Message(
            "Joonas",
            "TEST!"
        ),
        Message(
            "Joonas",
            "TEST!"
        ),
        Message(
            "Joonas",
            "TEST!"
        ),
    )
}

@Serializable
object Conversation

@Serializable
object Settings

data class Message(val author: String, val body: String)

const val CHANNEL_ID = "default_channel_id"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = AppDatabase.getDatabase(this)
        val userDao = db.userDao()

        var initialUser: User

        val users = userDao.getAll()
        if (users.isEmpty()) {
            userDao.insertUsers(User(username = "Joonas"))
            initialUser = userDao.getUser()
        } else {
            initialUser = userDao.getUser()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel.
            val name = "Default Notifications"
            val descriptionText = "Default Notifications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system. You can't change the importance
            // or other notification behaviors after this.
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }

        setContent {
            HW4SensorsAndNotificationsTheme {
                MyAppNavHost(initialUser, userDao)
            }
        }
    }
}

@Composable
fun MyAppNavHost(initialUser: User, userDao: UserDao) {
    val navController = rememberNavController()
    var user by remember { mutableStateOf(initialUser) }

    NavHost(
        navController = navController,
        startDestination = Conversation
    ) {
        composable<Conversation> {
            ConversationScreen(
                user,
                onNavigateToSettings = {
                    navController.navigate(Settings)
                }
            )
        }
        composable<Settings> {
            SettingsScreen(
                user = user,
                onProfileChange = { newProfile ->
                    userDao.updateUser(newProfile)  // updates existing row
                    user = newProfile                // update UI state
                },
                onNavigateBack = {
                    navController.navigate(Conversation) {
                        popUpTo(Conversation) {
                            inclusive = true
                        }
                    }
                }
            )
        }
    }
}

// More info about bars (I got the example here): https://developer.android.com/develop/ui/compose/components/app-bars
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    user: User,
    onNavigateToSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Conversation")
                },
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            painter = painterResource(R.drawable.settings_24px),
                            contentDescription = "Settings"
                        )
                    }
                }
            )
        },
    )
    { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Conversation(SampleData.conversationSample, user)
        }
    }
}

@Composable
fun Conversation(messages: List<Message>, user: User) {
    LazyColumn {
        items(messages) { message ->
            MessageCard(message, user)
        }
    }
}

@Composable
fun MessageCard(msg: Message, user: User) {
    Row(modifier = Modifier.padding(all = 8.dp)) {
        Image(
            painter = rememberAsyncImagePainter(user.imageUri),
            contentDescription = "Profile picture",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.secondary, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))

        // We keep track if the message is expanded or not in this
        // variable
        var isExpanded by remember { mutableStateOf(false) }
        // surfaceColor will be updated gradually from one color to the other
        val surfaceColor by animateColorAsState(
            if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
        )

        // We toggle the isExpanded variable when we click on this Column
        Column(modifier = Modifier.clickable { isExpanded = !isExpanded }) {
            Text(
                text = user.username,
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleSmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Surface(
                shape = MaterialTheme.shapes.medium,
                shadowElevation = 1.dp,
                // surfaceColor color will be changing gradually from primary to surface
                color = surfaceColor,
                // animateContentSize will change the Surface size gradually
                modifier = Modifier
                    .animateContentSize()
                    .padding(1.dp)
            ) {
                Text(
                    text = msg.body,
                    modifier = Modifier.padding(all = 4.dp),
                    // If the message is expanded, we display all its content
                    // otherwise we only display the first line
                    maxLines = if (isExpanded) Int.MAX_VALUE else 1,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

fun showNotification(context: Context) {

    val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_launcher_foreground)
        .setContentTitle("Test Notification")
        .setContentText("This is a test notification")
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)

    NotificationManagerCompat.from(context)
        .notify(1, builder.build())
}

// Got really big help from Philipp Lackner and his youtube video on how to use notifications:
// https://www.youtube.com/watch?v=bHlLYhSrXvc
@Composable
fun NotificationButton() {
    val context = LocalContext.current

    var hasNotificationPermission by remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            mutableStateOf(
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            )
        } else {
            // On Android versions (< API 33) where notification permission does NOT exist, we treat it as already granted
            mutableStateOf(true)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasNotificationPermission = isGranted
        }
    )

    Button(onClick = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        if (hasNotificationPermission) {
            showNotification(context)
        }
    }) {
        Text("Enable Notifications")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    user: User,
    onProfileChange: (User) -> Unit,
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("Settings")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
            )
        },
    )
    { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            ProfileImage(user, onProfileChange)

            Spacer(modifier = Modifier.height(8.dp))

            UsernameInput(user, onProfileChange)

            NotificationButton()
        }
    }
}

@Composable
fun ProfileImage(user: User, onProfileChange: (User) -> Unit) {
    val context = LocalContext.current

    val pickMedia =
        rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                val savedImageUri = savePickedImage(context, uri, "profile_picture.jpg")
                val uriWithVersion = savedImageUri.toString() + "?${System.currentTimeMillis()}"
                onProfileChange(user.copy(imageUri = uriWithVersion))
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    Column {
        Image(
            painter = rememberAsyncImagePainter(user.imageUri?.toUri()),
            contentDescription = "My Image",
            modifier = Modifier
                .size(128.dp)
                .clip(CircleShape)
                .border(1.5.dp, MaterialTheme.colorScheme.secondary, CircleShape)
                .clickable { pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }
        )
    }
}

fun savePickedImage(context: Context, contentUri: Uri, filename: String): Uri {
    val resolver = context.contentResolver

    resolver.openInputStream(contentUri).use { inputStream ->
        context.openFileOutput(filename, Context.MODE_PRIVATE).use { outputStream ->
            inputStream?.copyTo(outputStream)
        }
    }

    return File(context.filesDir, filename).toUri()
}


@Composable
fun UsernameInput(
    user: User,
    onProfileChange: (User) -> Unit
) {
    TextField(
        value = user.username,
        onValueChange = { newName ->
            onProfileChange(user.copy(username = newName))
        },
        label = { Text("Username") },
        singleLine = true,
    )
}


//@Preview
//@Composable
//fun PreviewConversation() {
//    HW4SensorsAndNotificationsTheme {
//        MyAppNavHost()
//    }
//}