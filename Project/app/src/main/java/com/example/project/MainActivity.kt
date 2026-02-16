package com.example.project

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
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
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import coil3.compose.rememberAsyncImagePainter
import com.example.project.api.OpenWeatherService
import com.example.project.ui.theme.ProjectTheme
import kotlinx.serialization.Serializable
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File

@Serializable
object Conversation

@Serializable
object Settings

const val CHANNEL_ID = "default_channel_id"
const val NOTIFICATION_ID = 1

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Got help to splashscreen implementation from Philipp Lackner:
        // https://www.youtube.com/watch?v=eFZmMSm1G1c
        installSplashScreen()

        val db = AppDatabase.getDatabase(this)
        val userDao = db.userDao()
        val messageDao = db.messageDao()

        var initialUser: User

        val users = userDao.getAll()
        if (users.isEmpty()) {
            userDao.insertUsers(User(username = "Joonas"))
            initialUser = userDao.getUser()
        } else {
            initialUser = userDao.getUser()
        }

        //INSERT TEST MESSAGE and get the list
//        messageDao.insertMessage(
//            MessageEntity(userId = initialUser.id, content = "Hello world!"))


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel.
            val name = "Default Notifications"
            val descriptionText = "Default Notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system. You can't change the importance
            // or other notification behaviors after this.
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }

        val sensor = SensorTest(applicationContext)
        sensor.init() // start listening

        setContent {
            ProjectTheme {
                MyAppNavHost(initialUser, userDao, messageDao)
            }
        }
    }
}

@Composable
fun MyAppNavHost(initialUser: User, userDao: UserDao, messageDao: MessageDao) {
    val navController = rememberNavController()
    var user by remember { mutableStateOf(initialUser) }
    var messages by remember {
        mutableStateOf(messageDao.getMessagesForUser(user.id))
    }

    // This is here so that the weather's state stays in the memory when switching views
    val weatherState = remember { mutableStateOf<WeatherModel?>(null) }

    NavHost(
        navController = navController,
        startDestination = Conversation
    ) {
        composable<Conversation> {
            ConversationScreen(
                user,
                messages,
                onNavigateToSettings = {
                    navController.navigate(Settings)
                },
                onNewMessage = { newContent ->
                    val newMessage = MessageEntity(userId = user.id, content = newContent)
                    messageDao.insertMessage(newMessage)
                    messages = messages + newMessage
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
                },
                weatherState
            )
        }
    }
}

// More info about bars (I got the example here): https://developer.android.com/develop/ui/compose/components/app-bars
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConversationScreen(
    user: User,
    messages: List<MessageEntity>,
    onNavigateToSettings: () -> Unit,
    onNewMessage: (String) -> Unit
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
        bottomBar = {
            NewMessage(onNewMessage)
        }
    )
    { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Conversation(messages,user)
        }
    }
}

@Composable
fun NewMessage(onNewMessage: (String) -> Unit){
    var textFieldValue by remember { mutableStateOf("") }

    Row(modifier = Modifier.fillMaxWidth())
    {
        TextField(
            value = textFieldValue,
            onValueChange =  { newValue -> textFieldValue = newValue },
            placeholder = { Text("Type a message") },
            modifier = Modifier.weight(1f)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Button(onClick = {
            if (!textFieldValue.isEmpty()){
                onNewMessage(textFieldValue)
                textFieldValue = "" // clear input
            }
        }) {
            Text("Send")
        }
    }
}

@Composable
fun Conversation(messages: List<MessageEntity>, user: User) {
    LazyColumn {
        items(messages) { message ->
            MessageCard(message, user)
        }
    }
}

@Composable
fun MessageCard(msg: MessageEntity, user: User) {
    Row(modifier = Modifier
        .padding(all = 8.dp)
        .fillMaxWidth() // this is added so the card is not depended on the text size
    ) {
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
                    text = msg.content,
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

// Got help from Philipp Lackner and his youtube video on how to use notifications:
// https://www.youtube.com/watch?v=bHlLYhSrXvc
@SuppressLint("MissingPermission")
@Composable
fun NotificationButton() {
    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                showNotification(context, "Notifications enabled")
            }
        }
    )

    Button(onClick = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            showNotification(context, "Notifications enabled")
        }
    }) {
        Text("Enable Notifications")
    }
}

@Composable
fun WeatherInfo(weatherState: MutableState<WeatherModel?>){
    Column {
        Button(onClick = {
            sendRequest(65.0121, 25.4651, OPENWEATHER_KEY, weatherState)
        }) {
            Text("Check the weather in Oulu")
        }
        Spacer(modifier = Modifier.height(8.dp))

        Text("Temperature: ${weatherState.value?.main?.temp}°C")
        Text("Humidity: ${weatherState.value?.main?.humidity} %")
    }
}

fun sendRequest(
    lat: Double,
    lon: Double,
    apiKey: String,
    weather : MutableState<WeatherModel?>
) {
    val retrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/data/2.5/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val api = retrofit.create(OpenWeatherService::class.java)
    val response = api.getCurrentWeather(lat, lon, apiKey)

    response.enqueue(object: Callback<WeatherModel>{
        override fun onResponse(call: Call<WeatherModel?>, response: Response<WeatherModel?>) {
            if(response.isSuccessful){
                Log.d("Main", "success!" + response.body().toString())
                weather.value = response.body()
            }
        }

        override fun onFailure(call: Call<WeatherModel?>, t: Throwable) {
            Log.e("Main", "Failed mate " + t.message.toString())
        }
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    user: User,
    onProfileChange: (User) -> Unit,
    onNavigateBack: () -> Unit,
    weatherState : MutableState<WeatherModel?>
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

            WeatherInfo(weatherState)
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
//    ProjectTheme {
//        MyAppNavHost()
//    }
//}