package uvg.edu.gt.smartfridge.data

object HttpRoutes {
    private const val BASE_URL = "127.0.0.1"

    // LOGIN REGISTER VIEW
    const val REGISTER= "/register_user"
    const val LOGIN = "/login_user"

    // HOME VIEW
    const val GET_RECIPES = "/get_recipes"
    const val SEARCH_RECIPES = "/search_recipes"

    // FRIDGE VIEW
    const val GET_INGREDIENTS = "/get_ingredients"
    const val SEARCH_INGREDIENTS = "/search_ingredients"

    // SETTINGS VIEW
    const val SAVE_SETTINGS = "/save_settings"

    // RECIPE VIEW
    const val GET_RECIPE_DETAILS = "/recipe_details"

    // ADD INGREDIENT VIEW
    const val ADD_INGREDIENT = "/add_ingredient"

    // EDIT INGREDIENT
    const val EDIT_INGREDIENT = "/edit_ingredient"
}