package uvg.edu.gt.smartfridge.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import uvg.edu.gt.smartfridge.R

// Definition of Outfit font family, the main app font.
val outfitFamily = FontFamily(
    Font(R.font.outfit_regular, FontWeight.Normal),
    Font(R.font.outfit_black, FontWeight.Black),
    Font(R.font.outfit_bold, FontWeight.Bold),
    Font(R.font.outfit_extrabold, FontWeight.ExtraBold),
    Font(R.font.outfit_semibold, FontWeight.SemiBold),
    Font(R.font.outfit_medium, FontWeight.Medium),
    Font(R.font.outfit_light, FontWeight.Light),
    Font(R.font.outfit_extralight, FontWeight.ExtraLight),
    Font(R.font.outfit_thin, FontWeight.Thin)
)

// Definition of Typography styles.
val appTypography = Typography(
    displayLarge = TextStyle(
        fontFamily = outfitFamily,
        fontSize = 44.sp,
        fontWeight = FontWeight.Black
    ), //44
    displayMedium = TextStyle(
        fontFamily = outfitFamily,
        fontSize = 24.sp,
        fontWeight = FontWeight.Black
    ), //24
    titleMedium = TextStyle(
        fontFamily = outfitFamily,
        fontSize = 24.sp,
        fontWeight = FontWeight.Normal
    ), // 32
    bodyLarge = TextStyle(
        fontFamily = outfitFamily,
        fontSize = 20.sp,
        fontWeight = FontWeight.Normal
    ), //20
    bodyMedium = TextStyle(
        fontFamily = outfitFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal
    ), // 16
    labelLarge = TextStyle(
        fontFamily = outfitFamily,
        fontSize = 20.sp,
        fontWeight = FontWeight.Normal
    ), // 16
    labelMedium = TextStyle(
        fontFamily = outfitFamily,
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal
    ), // 12
    labelSmall = TextStyle(
        fontFamily = outfitFamily,
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal
    ), // 8
)