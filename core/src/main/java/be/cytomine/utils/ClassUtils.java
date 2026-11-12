package be.cytomine.utils;

import org.hibernate.proxy.HibernateProxy;

public class ClassUtils {

    /**
     * Get the class name of an object without package name
     *
     * @param o Object
     *
     * @return Class name (without package) of o
     */
    public static String getClassName(Object o) {
        String name = o.getClass().getName();   //be.cytomine.image.Image
        int exeed = name.indexOf("_\\$\\$_javassist");
        if (exeed != -1) {
            name = name.substring(0, exeed); //if ex be.cytomine.image.Image_$$_javassistxxxx...remove all after  _$$
        } else if (o instanceof HibernateProxy hibernateProxy) {
            // if ex be.cytomine.image.Image$HibernateProxy$GfTtnrQ6, take the super class (Image)
            name = hibernateProxy.getClass().getSuperclass().getName();
        }
        String[] array = name.split("\\."); //[be,cytomine,image,Image]

        return array[array.length - 1]; // Image
    }
}
