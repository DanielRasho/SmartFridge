package uvg.edu.gt.smartfridge.models

data class Recipe(val title: String, val banner: String, val tags: Sequence<String>, val ingredients: Sequence<Ingredient>, val source: String)
