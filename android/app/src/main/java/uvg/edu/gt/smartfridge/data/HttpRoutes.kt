package uvg.edu.gt.smartfridge.data

object HttpRoutes {
    private const val BASE_URL = "127.0.0.1"

    // LOGIN REGISTER VIEW
    const val REGISTER= "/user/register"
    const val LOGIN = "/user/login"

    // HOME VIEW
    const val GET_RECIPES = "/recipes"
    const val SEARCH_RECIPES = "/recipes/search"

    // FRIDGE VIEW
    const val GET_INGREDIENTS = "/ingredients"
    const val SEARCH_INGREDIENTS = "/ingredients/search"

    // SETTINGS VIEW
    const val LOGOUT = "/user/logout"
    const val SAVE_SETTINGS = "/settings/save"

    // RECIPE VIEW
    const val GET_RECIPE_DETAILS = "/recipe/details"

    // ADD INGREDIENT VIEW
    const val ADD_INGREDIENT = "/ingredients/add"

    // EDIT INGREDIENT
    const val EDIT_INGREDIENT = "/ingredients/edit"
    const val DELETE_INGREDIENT = "/ingredients/remove"
}