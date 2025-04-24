#include "gamescene.h"
#include <QRandomGenerator>

gamescene::gamescene(QObject *parent) : QGraphicsScene(parent) {
    // Set the scene rectangle
    setSceneRect(0, 0, 908, 510);

    // Set background image
    setBackgroundBrush(QBrush(QImage(":/images/background.jpg").scaledToHeight(512).scaledToWidth(910)));

    // Create and add clouds to the scene
    cloud *cloud1 = new cloud();
    cloud *cloud2 = new cloud();
    cloud *cloud3 = new cloud();

    cloud1->setPos(100, 50);
    cloud2->setPos(350, 30);
    cloud3->setPos(600, 60);

    addItem(cloud1);
    addItem(cloud2);
    addItem(cloud3);

    // Create and add bucket to the scene
    bucketItem = new bucket();
    addItem(bucketItem);

    // Set initial position of the bucket
    bucketItem->setPos(400, 365);

    // Make bucket focusable and set focus to it
    bucketItem->setFlag(QGraphicsItem::ItemIsFocusable);
    bucketItem->setFocus();

    // Create timer for spawning water droplets
    dropletTimer = new QTimer(this);
    connect(dropletTimer, &QTimer::timeout, this, &gamescene::spawnDroplet);
    dropletTimer->start(1000); // Spawn a new droplet every second
}

void gamescene::spawnDroplet() {
    // Create a new water droplet at a random x position
    int randomX = QRandomGenerator::global()->bounded(30, 878);
    water *droplet = new water();
    droplet->setPos(randomX, 50); // Start from top of the screen
    addItem(droplet);
}
