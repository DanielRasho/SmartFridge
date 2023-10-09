use std::{net::SocketAddr, sync::Arc};

use axum::{routing::{post, get}, Router};
use backend::routes::{
    get_ingredients::get_ingredients, get_recipes::get_recipes, login_user::login_user,
    logout::logout, recipe_details::recipe_details, register_user::register_user,
    save_settings::save_settings, search_ingredients::search_ingredients,
    search_recipes::search_recipes, add_ingredient::add_ingredient,
};
use tokio_postgres::{Client, Error};
use tower_http::cors::{Any, CorsLayer};
use tracing_subscriber::{layer::SubscriberExt, util::SubscriberInitExt};

const DB_CONNECTION_CONFIG: &str =
    "host=localhost port=5432 user=postgres dbname=lab04 connect_timeout=10";
pub const APP_SECRET: &[u8] = b"super-secret-key";

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

    //let (client, connection) = tokio_postgres::connect(DB_CONNECTION_CONFIG, NoTls).await?;

    //let db_connection_handle = tokio::spawn(async {
    //connection.await
    //});

    start_server_on(addr, Arc::new(None)).await;

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
    Router::new()
        .route("/register_user", post(register_user))
        .route("/login_user", post(login_user))
        .route("/logout", post(logout))
        .route("/get_recipes", get(get_recipes))
        .route("/search_recipes", get(search_recipes))
        .route("/get_ingredients", get(get_ingredients))
        .route("/search_ingredients", get(search_ingredients))
        .route("/save_settings", post(save_settings))
        .route("/recipe_details", get(recipe_details))
        .route("/add_ingredient", post(add_ingredient))
}
