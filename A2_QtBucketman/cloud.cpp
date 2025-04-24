#include "cloud.h"

cloud::cloud(QObject *parent) : QObject(parent), movingRight(true) {
    // Set the cloud image and scale it
    setPixmap((QPixmap(":/images/cloud.png")).scaled(120, 80));

    // Create a timer to move the cloud
    moveTimer = new QTimer(this);
    connect(moveTimer, &QTimer::timeout, this, &cloud::moveCloud);
    moveTimer->start(100);
}

void cloud::moveCloud() {
    if (movingRight) {
        setPos(x() + 2, y());
        if (x() > 800) {
            movingRight = false;
        }
    } else {
        setPos(x() - 2, y());
        if (x() < 10) {
            movingRight = true;
        }
    }
}
