package butterknife;

import butterknife.internal.ListenerClass;
import butterknife.internal.ListenerMethod;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static android.widget.AdapterView.OnItemClickListener;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.CLASS;

/**
 * Bind a method to an {@link android.widget.AdapterView.OnItemClickListener OnItemClickListener} on the view for each ID
 * specified.
 * <pre><code>
 * {@literal @}OnItemClick(R.id.example_list) void onItemClick(int position) {
 *   Toast.makeText(this, "Clicked position " + position + "!", LENGTH_SHORT).show();
 * }
 * </code></pre>
 * Any number of parameters from {@link android.widget.AdapterView.OnItemClickListener#onItemClick(android.widget.AdapterView,
 * android.view.View, int, long) onItemClick} may be used on the method.
 *
 * @see android.widget.AdapterView.OnItemClickListener
 * @see butterknife.Optional
 */
@Target(METHOD)
@Retention(CLASS)
@ListenerClass(
    targetType = "android.widget.AdapterView<?>",
    setter = "setOnItemClickListener",
    type = "android.widget.AdapterView.OnItemClickListener",
    method = @ListenerMethod(
        name = "onItemClick",
        parameters = {
            "android.widget.AdapterView<?>",
            "android.view.View",
            "int",
            "long"
        }
    )
)
public @interface OnItemClick {
  /** View IDs to which the method will be bound. */
  int[] value();
}
