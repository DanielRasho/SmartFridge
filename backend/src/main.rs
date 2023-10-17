use std::{net::SocketAddr, sync::Arc};

use axum::{
    routing::{get, post},
    Router,
};
use backend::routes::{
    add_ingredient::add_ingredient, edit_ingredient::edit_ingredient,
    get_ingredients::get_ingredients, get_recipes::get_recipes, login_user::login_user,
    logout::logout, recipe_details::recipe_details, register_user::register_user,
    remove_ingredient::remove_ingredient, save_settings::save_settings,
    search_ingredients::search_ingredients, search_recipes::search_recipes,
};
use clap::Parser;
use tokio_postgres::{Client, Error};
use tower_http::cors::{Any, CorsLayer};
use tracing_subscriber::{layer::SubscriberExt, util::SubscriberInitExt};

const DB_CONNECTION_CONFIG: &str =
    "host=localhost port=5432 user=postgres dbname=smart_fridge connect_timeout=10";

#[derive(Debug, Parser)]
#[command(author, version, about, long_about = None)]
struct Params {
    /// The host to start the server.
    /// The default host is: 127.0.0.1
    /// Also known as localhost.
    host: Option<String>,

    /// The port to start the server. Make sure this port is free before starting.
    /// The default port is: 3000
    port: Option<String>,

    /// The connection string to connect to the database.
    /// Default is: host=localhost port=5432 user=postgres dbname=smart_fridge connect_timeout=10
    db_connection: Option<String>,
}

#[tokio::main]
async fn main() -> Result<(), Error> {
    let params = Params::parse();

    let host = match &params.host {
        Some(v) => v,
        None => "127.0.0.1",
    };

    let port = match &params.port {
        Some(v) => v,
        None => "3000",
    };

    let db_config = match &params.db_connection {
        Some(v) => v,
        None => "host=localhost port=5432 user=postgres dbname=smart_fridge connect_timeout=10",
    };

    tracing_subscriber::registry()
        .with(
            tracing_subscriber::EnvFilter::try_from_default_env()
                .unwrap_or_else(|_| "backend=debug,tower_http=debug".into()),
        )
        .with(tracing_subscriber::fmt::layer())
        .init();

    let addr = format!("{}:{}", host, port);
    let addr: SocketAddr = addr.parse().unwrap();

    let (client, connection) = tokio_postgres::connect(db_config, tokio_postgres::NoTls).await?;

    tokio::spawn(async move {
        if let Err(e) = connection.await {
            tracing::error!(
                "An error `{:?}` occurred connecting to DB: `{}`",
                e,
                DB_CONNECTION_CONFIG
            );
        }
    });

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
    let db_c_11 = db_client.clone();

    Router::new()
        .route("/user/register", post(|p| register_user(p, db_client)))
        .route("/user/login", post(|p| login_user(p, db_c_1)))
        .route("/user/logout", post(|p| logout(p, db_c_2)))
        .route("/settings/save", post(|p| save_settings(p, db_c_7)))
        // Recipes
        .route("/recipes", get(|p| get_recipes(p, db_c_3)))
        .route("/recipes/search", get(|p| search_recipes(p, db_c_4)))
        .route("/recipes/details", get(|p| recipe_details(p, db_c_8)))
        // Ingredients
        .route("/ingredients", get(|p| get_ingredients(p, db_c_5)))
        .route("/ingredients/add", post(|p| add_ingredient(p, db_c_9)))
        .route("/ingredients/edit", post(|p| edit_ingredient(p, db_c_10)))
        .route(
            "/ingredients/remove",
            post(|p| remove_ingredient(p, db_c_11)),
        )
        .route(
            "/ingredients/search",
            get(|p| search_ingredients(p, db_c_6)),
        )
}
