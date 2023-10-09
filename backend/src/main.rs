use std::{net::SocketAddr, sync::Arc};

use axum::{routing::post, Router};
use backend::routes::{login_user::login_user, register_user::register_user, search_recipes::search_recipes, get_recipes::get_recipes, get_ingredients::get_ingredients, search_ingredients::search_ingredients};
use tokio_postgres::{Client, Error};
use tower_http::cors::{Any, CorsLayer};
use tracing_subscriber::{layer::SubscriberExt, util::SubscriberInitExt};

const DB_CONNECTION_CONFIG: &str =
    "host=localhost port=5432 user=postgres dbname=lab04 connect_timeout=10";

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
        .route("/get_recipes", post(get_recipes))
        .route("/search_recipes", post(search_recipes))
        .route("/get_ingredients", post(get_ingredients))
        .route("/search_ingredients", post(search_ingredients))
}
