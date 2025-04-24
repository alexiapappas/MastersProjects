#include "bucket.h"

bucket::bucket(QObject *parent) : QObject(parent) {
    setPixmap((QPixmap(":images/bucket.png")).scaled(150, 150));
}

void bucket::keyPressEvent(QKeyEvent *event) {
    // Move bucket left and right with arrow keys
    if (event->key() == Qt::Key_Left) {
        // Make sure the bucket doesn't go off the left edge of the scene
        if (x() > 0) {
            setPos(x()- 20, y());
        }
    } else if (event->key() == Qt::Key_Right) {
        // Make sure the bucket doesn't go off the right edge of the scene
        if (x() < 758) {
            setPos(x() + 20, y());
        }
    }
}
