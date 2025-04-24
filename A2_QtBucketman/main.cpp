#include <QApplication>
#include <QGraphicsView>
#include "gamescene.h"

int main(int argc, char *argv[]) {
    QApplication a(argc, argv);

    // Create the game scene
    gamescene *scene1 = new gamescene();

    // Create a view to visualize the scene
    QGraphicsView *view = new QGraphicsView();
    view->setScene(scene1);

    // Fix the view size
    view->setFixedSize(910, 512);

    // Disable scroll bars
    view->setHorizontalScrollBarPolicy(Qt::ScrollBarAlwaysOff);
    view->setVerticalScrollBarPolicy(Qt::ScrollBarAlwaysOff);

    view->show();

    return a.exec();
}
