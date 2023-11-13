#![recursion_limit = "256"]
use std::{io, net::SocketAddr, sync::Arc};

use axum::{routing::post, Router};
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

#[derive(Debug, Parser)]
#[command(author, version, about, long_about = None)]
struct Params {
    /// The host to start the server on. Check the port is free before starting the server.
    #[arg(short, long, env, default_value = "127.0.0.1:3000", value_parser = resolve_host)]
    server_host: SocketAddr,

    /// The connection string to connect to the database.
    #[arg(
        short,
        long,
        env,
        default_value = "host=localhost port=5432 user=postgres dbname=smart_fridge connect_timeout=10"
    )]
    db_connection: String,
}

fn resolve_host(host: &str) -> io::Result<SocketAddr> {
    let host: SocketAddr = host.parse().map_err(|_| {
        io::Error::new(
            io::ErrorKind::AddrNotAvailable,
            format!("Couldn't find destination {host}"),
        )
    })?;
    Ok(host)
}

#[tokio::main]
async fn main() -> Result<(), Error> {
    let params = Params::parse();

    tracing_subscriber::registry()
        .with(
            tracing_subscriber::EnvFilter::try_from_default_env()
                .unwrap_or_else(|_| "backend=debug,tower_http=debug".into()),
        )
        .with(tracing_subscriber::fmt::layer())
        .init();

    tracing::debug!("Connecting to DB...");

    let (client, connection) =
        tokio_postgres::connect(&params.db_connection, tokio_postgres::NoTls).await?;

    tokio::spawn(async move {
        if let Err(e) = connection.await {
            tracing::error!(
                "An error `{:?}` occurred connecting to DB: `{}`",
                e,
                params.db_connection
            );
        }
    });
    tracing::debug!("Connection with DB established!");

    start_server_on(params.server_host, Arc::new(Some(client))).await;

    Ok(())
}

/// Starts a server on the specified address
async fn start_server_on(addr: SocketAddr, client: Arc<Option<Client>>) {
    tracing::debug!("Listening on `{}` ...", addr);

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
        .route("/recipes", post(|p| get_recipes(p, db_c_3)))
        .route("/recipes/search", post(|p| search_recipes(p, db_c_4)))
        .route("/recipes/details", post(|p| recipe_details(p, db_c_8)))
        // Ingredients
        .route("/ingredients", post(|p| get_ingredients(p, db_c_5)))
        .route("/ingredients/add", post(|p| add_ingredient(p, db_c_9)))
        .route("/ingredients/edit", post(|p| edit_ingredient(p, db_c_10)))
        .route(
            "/ingredients/remove",
            post(|p| remove_ingredient(p, db_c_11)),
        )
        .route(
            "/ingredients/search",
            post(|p| search_ingredients(p, db_c_6)),
        )
}
