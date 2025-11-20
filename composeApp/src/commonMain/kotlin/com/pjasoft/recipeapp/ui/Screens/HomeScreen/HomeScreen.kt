package com.pjasoft.recipeapp.ui.Screens.HomeScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.material3.BottomSheetDefaults.DragHandle
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.pjasoft.recipeapp.domain.dtos.Prompt
import com.pjasoft.recipeapp.domain.dtos.RecipeDTO
import com.pjasoft.recipeapp.domain.utils.Preferences
import com.pjasoft.recipeapp.domain.utils.hideKeyboard
import com.pjasoft.recipeapp.ui.Components.CustomOutlinedTextField
import com.pjasoft.recipeapp.ui.Components.LoadingOverlay
import com.pjasoft.recipeapp.ui.RecipeTheme
import com.pjasoft.recipeapp.ui.Screens.HomeScreen.Components.GeneratedRecipe
import com.pjasoft.recipeapp.ui.Screens.HomeScreen.Components.Header
import com.pjasoft.recipeapp.ui.Screens.HomeScreen.Components.RecipeCard
import com.pjasoft.recipeapp.ui.Screens.HomeScreen.Components.RecipeItems
import com.pjasoft.recipeapp.ui.Screens.HomeScreenRoute
import com.pjasoft.recipeapp.ui.Screens.LoginScreenRoute
import com.pjasoft.recipeapp.ui.viewModels.RecipeViewModel
import kotlinx.coroutines.launch
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.reflect.KClass

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {

    val colors = MaterialTheme.colorScheme
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = false
    )

    var prompt by remember { mutableStateOf("") }

    val viewModel: RecipeViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
                return RecipeViewModel() as T
            }
        }
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
            .safeContentPadding()
            .padding(5.dp)
    ) {

        // HEADER
        item {
            Header(
                onLoguot = {
                    Preferences.clearSettings()
                    navController.navigate(LoginScreenRoute) {
                        popUpTo(HomeScreenRoute) { inclusive = true }
                    }
                }
            )
        }

        // INPUT PARA GENERAR RECETA
        item {
            Text(
                text = "Crea, Cocina, Comparte y disfruta",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold
                )
            )
            Spacer(modifier = Modifier.height(10.dp))

            CustomOutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = prompt,
                onValueChange = { prompt = it },
                trailingIcon = Icons.Default.AutoAwesome,
                placeHolder = "Escribe tus ingredientes...",
                onTrailingIconClick = {
                    hideKeyboard(focusManager)
                    viewModel.generateRecipe(Prompt(prompt))
                    scope.launch { sheetState.partialExpand() }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        hideKeyboard(focusManager)
                        viewModel.generateRecipe(Prompt(prompt))
                        scope.launch { sheetState.partialExpand() }
                    }
                )
            )
        }

        // TUS RECETAS RECIENTES
        item {
            Text(
                text = "Tus recetas recientes",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(viewModel.recipes) { recipe ->

                    val dto = RecipeDTO(
                        title = recipe.title,
                        category = recipe.category,
                        minutes = recipe.minutes,
                        stars = recipe.stars,
                        ingredients = recipe.ingredients,
                        instructions = recipe.instructions,
                        imageUrl = recipe.imageUrl ?: "",
                        prompt = ""
                    )

                    Card(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp)), // Quitamos el .clickable de aquí
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 4.dp
                        )
                    ) {
                        RecipeCard(
                            recipe = dto,
                            onClick = { // <--- Aquí es donde faltaba el parámetro
                                scope.launch {
                                    viewModel.showModalFromList(dto)
                                    sheetState.partialExpand()
                                }
                            }
                        )
                    }
                }
            }
        }

        // IDEAS RÁPIDAS
        item {
            val tags = listOf("Rápidas (10 min)", "Pocas calorías", "Sin horno", "Desayunos")

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Ideas Rápidas", style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ))
            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                items(tags) { tag ->
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colors.primary.copy(alpha = 0.1f))
                            .padding(horizontal = 20.dp, vertical = 10.dp)
                            .clickable {}
                    ) {
                        Text(
                            text = tag,
                            color = colors.primary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.primary.copy(alpha = 0.1f))
                    .padding(20.dp)
                    .clickable {},
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "¿No sabes qué cocinar hoy?", style = MaterialTheme.typography.bodyLarge)
                    Text(text = "Genera una receta aleatoria", style = MaterialTheme.typography.bodyMedium)
                }
                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = colors.primary)
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        // TODAS TUS RECETAS
        item {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Todas tus recetas",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        items(viewModel.recipes) { recipe ->
            val dto = RecipeDTO(
                title = recipe.title,
                category = recipe.category,
                minutes = recipe.minutes,
                stars = recipe.stars,
                ingredients = recipe.ingredients,
                instructions = recipe.instructions,
                imageUrl = recipe.imageUrl ?: "",
                prompt = ""
            )

            RecipeItems(dto) {
                scope.launch {
                    viewModel.showModalFromList(dto)
                    sheetState.partialExpand()
                }
            }
        }
    }

    // MODAL CORRECTAMENTE CONFIGURADO
    if (viewModel.showSheet) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.hideModal() },
            sheetState = sheetState,
            dragHandle = { DragHandle() },
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            GeneratedRecipe(
                recipe = viewModel.generatedRecipe,
                onSave = {
                    scope.launch {
                        viewModel.saveRecipeInDb()
                        viewModel.hideModal()
                        sheetState.hide()
                    }
                }
            )
        }
    }

    // LOADING
    if (viewModel.isLoading) {
        LoadingOverlay(colors)
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    RecipeTheme {
        HomeScreen(rememberNavController())
    }
}