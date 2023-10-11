use std::{net::SocketAddr, sync::Arc};

use axum::{
    routing::{get, post},
    Router,
};
use backend::routes::{
    add_ingredient::add_ingredient, edit_ingredient::edit_ingredient,
    get_ingredients::get_ingredients, get_recipes::get_recipes, login_user::login_user,
    logout::logout, recipe_details::recipe_details, register_user::register_user,
    save_settings::save_settings, search_ingredients::search_ingredients,
    search_recipes::search_recipes,
};
use tokio_postgres::{Client, Error};
use tower_http::cors::{Any, CorsLayer};
use tracing_subscriber::{layer::SubscriberExt, util::SubscriberInitExt};

const DB_CONNECTION_CONFIG: &str =
    "host=localhost port=5432 user=postgres dbname=smart_fridge connect_timeout=10";

#[tokio::main]
async fn main() -> Result<(), Error> {
    tracing_subscriber::registry()
        .with(
            tracing_subscriber::EnvFilter::try_from_default_env()
                .unwrap_or_else(|_| "backend=debug,tower_http=debug".into()),
        )
        .with(tracing_subscriber::fmt::layer())
        .init();

    let addr = if cfg!(debug_assertions) {
        SocketAddr::from(([127, 0, 0, 1], 3000))
    } else {
        SocketAddr::from(([0, 0, 0, 0], 3000))
    };

    let (client, connection) =
        tokio_postgres::connect(DB_CONNECTION_CONFIG, tokio_postgres::NoTls).await?;

    let _ = tokio::spawn(async { connection.await });

    start_server_on(addr, Arc::new(Some(client))).await;

    Ok(())
}

/// Starts a server on the specified address
async fn start_server_on(addr: SocketAddr, client: Arc<Option<Client>>) {
    tracing::debug!("listening on {}", addr);

    let cors = if cfg!(debug_assertions) {
        CorsLayer::new()
            .allow_methods(Any)
            .allow_headers(Any)
            .allow_origin(Any)
    } else {
        CorsLayer::new()
    };

    axum::Server::bind(&addr)
        .serve(app(client.clone()).layer(cors).into_make_service())
        .await
        .unwrap();
}

/// Having a function that produces our app makes it easy to call it from tests
/// without having to create an HTTP server.
#[allow(dead_code)]
fn app(db_client: Arc<Option<Client>>) -> Router {
    let db_c_1 = db_client.clone();
    let db_c_2 = db_client.clone();
    let db_c_3 = db_client.clone();
    let db_c_4 = db_client.clone();
    let db_c_5 = db_client.clone();
    let db_c_6 = db_client.clone();
    let db_c_7 = db_client.clone();
    let db_c_8 = db_client.clone();
    let db_c_9 = db_client.clone();
    let db_c_10 = db_client.clone();

    Router::new()
        .route("/register_user", post(|p| register_user(p, db_client)))
        .route("/login_user", post(|p| login_user(p, db_c_1)))
        .route("/logout", post(|p| logout(p, db_c_2)))
        .route("/get_recipes", get(|p| get_recipes(p, db_c_3)))
        .route("/search_recipes", get(|p| search_recipes(p, db_c_4)))
        .route("/get_ingredients", get(|p| get_ingredients(p, db_c_5)))
        .route(
            "/search_ingredients",
            get(|p| search_ingredients(p, db_c_6)),
        )
        .route("/save_settings", post(|p| save_settings(p, db_c_7)))
        .route("/recipe_details", get(|p| recipe_details(p, db_c_8)))
        .route("/add_ingredient", post(|p| add_ingredient(p, db_c_9)))
        .route("/edit_ingredient", post(|p| edit_ingredient(p, db_c_10)))
}
