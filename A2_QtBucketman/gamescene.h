#ifndef GAMESCENE_H
#define GAMESCENE_H

#include <QGraphicsScene>
#include <QGraphicsPixmapItem>
#include <QBrush>
#include <QImage>
#include <QTimer>
#include "bucket.h"
// #include "water.h"
// #include "cloud.h"

class gamescene : public QGraphicsScene {
    Q_OBJECT

public:
    explicit gamescene(QObject *parent = nullptr);

private:
    bucket *bucketItem;
    QTimer *dropletTimer;

private slots:
    void spawnDroplet();
};

#endif // GAMESCENE_H
