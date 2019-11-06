package com.github.fxrouter;

/*
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.
 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import javafx.animation.FadeTransition;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.AbstractMap;
import java.util.HashMap;

/**
 * FXRouter allows to manage scenes switching on JavaFX Application with an easy API
 * Inspired by Angular JS $routeProvider
 *
 * @author Marco Trombino
 * @version 1.0.0
 */
@Slf4j
public final class FXRouter {
    private static final String WINDOW_TITLE = "";
    private static final Double WINDOW_WIDTH = 800.0;
    private static final Double WINDOW_HEIGHT = 600.0;
    private static final Double FADE_ANIMATION_DURATION = 800.0;

    // FXRouter Singleton
    private static FXRouter router;

    // FXRouter Application Stage reference to set scenes
    private static Stage window;

    // Application Stage title
    private static String windowTitle;
    // Application Stage size
    private static Double windowWidth;
    private static Double windowHeight;

    // routes switching animation
    private static String animationType;
    private static Double animationDuration;

    // FXRouter routes map
    private static AbstractMap<String, RouteScene> routes = new HashMap<>();
    // FXRouter current route
    private static RouteScene currentRoute;

    /**
     * FXRouter constructor kept private to apply Singleton pattern
     */
    private FXRouter() {
    }

    /**
     * FXRouter binder with Application Stage and main package
     *
     * @param win:       Application Stage
     */
    public static void bind(Stage win) {
        bind(win, WINDOW_TITLE, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    /**
     * FXRouter binder with Application Stage and main package
     *
     * @param win:       Application Stage
     * @param winTitle:  Application Stage title
     */
    public static void bind(Stage win, String winTitle) {
        bind(win, winTitle, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    /**
     * FXRouter binder with Application Stage and main package
     *
     * @param win:       Application Stage
     * @param winTitle:  Application Stage title
     * @param winWidth:  Application Stage width
     * @param winHeight: Application Stage height
     */
    public static void bind(Stage win, String winTitle, double winWidth, double winHeight) {
        checkInstances(win);
        windowTitle = winTitle;
        windowWidth = winWidth;
        windowHeight = winHeight;
    }

    /**
     * set FXRouter references only if they are not set yet
     *
     * @param win: Application Stage
     */
    private static void checkInstances(Stage win) {
        if (router == null)
            router = new FXRouter();
        if (window == null)
            window = win;
    }

    /**
     * Define a FXRouter route
     *
     * @param routeLabel:  Route label identifier
     * @param scenePath:   .FXML scene file
     */
    public static void when(String routeLabel, String scenePath) {
        when(routeLabel, scenePath, WINDOW_TITLE);
    }

    /**
     * Define a FXRouter route
     *
     * @param routeLabel:  Route label identifier
     * @param scenePath:   .FXML scene file
     * @param winTitle:    Scene (Stage) title
     */
    public static void when(String routeLabel, String scenePath, String winTitle) {
        when(routeLabel, scenePath, winTitle, WINDOW_WIDTH, WINDOW_HEIGHT);
    }

    /**
     * Define a FXRouter route
     *
     * @param routeLabel:  Route label identifier
     * @param scenePath:   .FXML scene file
     * @param winTitle:    Scene (Stage) title
     * @param sceneWidth:  Scene Width
     * @param sceneHeight: Scene Height
     */
    public static void when(String routeLabel, String scenePath, String winTitle, double sceneWidth, double sceneHeight) {
        RouteScene routeScene = new RouteScene(scenePath, winTitle, sceneWidth, sceneHeight);
        routes.put(routeLabel, routeScene);
    }

    /**
     * Switch between FXRouter route and show corresponding scenes
     *
     * @param routeLabel: Route label identifier
     * @throws IOException: throw FXMLLoader exception if file is not loaded correctly
     */
    public static void goTo(String routeLabel) throws IOException {
        // get corresponding route
        RouteScene route = routes.get(routeLabel);
        loadNewRoute(route);
    }

    /**
     * Switch between FXRouter route and show corresponding scenes
     *
     * @param routeLabel: Route label identifier
     * @param data:       Data passed to route
     * @throws IOException: throw FXMLLoader exception if file is not loaded correctly
     */
    public static void goTo(String routeLabel, Object data) throws IOException {
        // get corresponding route
        RouteScene route = routes.get(routeLabel);
        // set route data
        route.data = data;
        loadNewRoute(route);
    }

    /**
     * Helper method of goTo() which load and show new scene
     *
     * @throws IOException: throw FXMLLoader exception if file is not loaded correctly
     */
    private static void loadNewRoute(RouteScene route) throws IOException {

        // set FXRouter current route reference
        currentRoute = route;

        // create correct file path.  "/" doesn't affect any OS
        String scenePath = route.scenePath;

        // load .fxml resource
        log.info(scenePath);
        Parent resource = FXMLLoader.load(new Object() {
        }.getClass().getResource(scenePath));

        // set window title from route settings or default setting
        window.setTitle(route.windowTitle);
        // set new route scene
        window.setScene(new Scene(resource, route.sceneWidth, route.sceneHeight));
        // show the window
        window.show();

        // set scene animation
        routeAnimation(resource);
    }

    /* Syntactic sugar for goTo() method when FXRouter get set */
    public static void startFrom(String routeLabel) throws Exception {
        goTo(routeLabel);
    }

    public static void startFrom(String routeLabel, Object data) throws Exception {
        goTo(routeLabel, data);
    }

    /**
     * set FXRouter switching animation
     *
     * @param anType: Animation type
     */
    public static void setAnimationType(String anType) {
        animationType = anType;
    }

    /**
     * set FXRouter switching animation
     *
     * @param anType:     Animation type
     * @param anDuration: Animation duration
     */
    public static void setAnimationType(String anType, double anDuration) {
        animationType = anType;
        animationDuration = anDuration;
    }

    /**
     * Animate routes switching based on animation type
     *
     * @param node: .FXML scene file to animate
     */
    private static void routeAnimation(Parent node) {
        String anType = animationType != null ? animationType.toLowerCase() : "";
        switch (anType) {
            case "fade":
                Double fd = animationDuration != null ? animationDuration : FADE_ANIMATION_DURATION;
                FadeTransition ftCurrent = new FadeTransition(Duration.millis(fd), node);
                ftCurrent.setFromValue(0.0);
                ftCurrent.setToValue(1.0);
                ftCurrent.play();
                break;
            default:
                break;
        }
    }

    /**
     * Get current route data
     */
    public static Object getData() {
        return currentRoute.data;
    }


    /**
     * FXRouter Inner Class used into routes map
     */
    private static class RouteScene {
        // route .fxml Scene path
        private String scenePath;
        // Scene (Stage) title
        private String windowTitle;
        private double sceneWidth;
        private double sceneHeight;
        // route data passed from goTo()
        private Object data;

        /**
         * Route scene constructor
         *
         * @param scenePath:   .FXML scene file
         */
        private RouteScene(String scenePath) {
            this(scenePath, getWindowTitle(), getWindowWidth(), getWindowHeight());
        }

        /**
         * Route scene constructor
         *
         * @param scenePath:   .FXML scene file
         * @param windowTitle: Scene (Stage) title
         */
        private RouteScene(String scenePath, String windowTitle) {
            this(scenePath, windowTitle, getWindowWidth(), getWindowHeight());
        }

        /**
         * Route scene constructor
         *
         * @param scenePath:   .FXML scene file
         * @param sceneWidth:  Scene Width
         * @param sceneHeight: Scene Height
         */
        private RouteScene(String scenePath, double sceneWidth, double sceneHeight) {
            this(scenePath, getWindowTitle(), sceneWidth, sceneHeight);
        }

        /**
         * Route scene constructor
         *
         * @param scenePath:   .FXML scene file
         * @param windowTitle: Scene (Stage) title
         * @param sceneWidth:  Scene Width
         * @param sceneHeight: Scene Height
         */
        private RouteScene(String scenePath, String windowTitle, double sceneWidth, double sceneHeight) {
            this.scenePath = scenePath;
            this.windowTitle = windowTitle;
            this.sceneWidth = sceneWidth;
            this.sceneHeight = sceneHeight;
        }

        private static String getWindowTitle() {
            return FXRouter.windowTitle != null ? FXRouter.windowTitle : WINDOW_TITLE;
        }

        private static double getWindowWidth() {
            return FXRouter.windowWidth != null ? FXRouter.windowWidth : WINDOW_WIDTH;
        }

        private static double getWindowHeight() {
            return FXRouter.windowHeight != null ? FXRouter.windowHeight : WINDOW_HEIGHT;
        }
    }
}
