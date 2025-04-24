#include <QList>
#include <QGraphicsScene>
#include "bucket.h"
#include "water.h"

water::water(QObject *parent) : QObject(parent) {
    // Set water droplet image and scale it
    setPixmap((QPixmap(":/images/water.gif")).scaled(30, 30));

    // Create a timer to move the droplet down
    timerDrop = new QTimer(this);
    connect(timerDrop, &QTimer::timeout, this, &water::moveDroplet);
    timerDrop->start(50);
}

void water::moveDroplet() {
    setPos(x(), y() + 5);

    // Check for collision with bucket
    QList<QGraphicsItem *> collidedItems = collidingItems();
    for (int i = 0; i < collidedItems.size(); ++i) {
        if (dynamic_cast<bucket*>(collidedItems[i])) {
            // Collision with bucket detected
            if (scene()) {
                scene()->removeItem(this);
            }
            delete this;
            return;
        }
    }

    // Delete the droplet if it goes out of the scene
    if (y() > 500) {
        if (scene()) {
            scene()->removeItem(this);
        }
            delete this;
    }
}
