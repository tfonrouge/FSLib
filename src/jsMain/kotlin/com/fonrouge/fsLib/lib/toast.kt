package com.fonrouge.fsLib.lib

import com.fonrouge.fsLib.model.state.ISimpleState
import io.kvision.toast.Toast
import io.kvision.toast.ToastOptions

/**
 * https://github.com/CodeSeven/toastr
 */
@Suppress("unused")
fun ISimpleState.toast() {
    when (isOk) {
        true -> Toast.success(
            message = msgOk ?: "Ok",
            options = ToastOptions(
                avatar = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAYAAABXAvmHAAAACXBIWXMAAAsTAAALEwEAmpwYAAAE1ElEQVR4nO2Z20/aZxjHf7FzUUQrIOdjO2tndrlsaa+aieIJD4iclZPGZX/BFm+I1RXQFpGjHLQ6TxXQJu26XnTr9ZJtzbbUJm2autlmWYv0WnrzLC9uxk6FH/IDtsRv8l7z+T487/s+7/eHYSc60YlyV0RxShgxXxSuGocFq6Y1/ophQ7BifM1b0b/hLfe/4S7pX3MX+h7yFvrWuAu6Ye6C7gJmsZRgxdZ7N418YWTAJoqaXwgjJhCumkBwwwiCFSPwVwzAX9YDb0kPvMX+1OIu9AH3Kx1w5nXAntM8Z81prdygjldw8NqIiS6MmgPC6EBSFDVDtvCceS2w57TAvq4B5ow6yZxV+dkBTU1B4M/GBrSimDkhig5ArvCsWfXumlEBI6TaZgaU6ryBfxgYKhXFBkOiGAInFp4ZVgEzpARGSAH0oGIaCwyVEgrPvjVEOhMb+Cbf8IygAuiBXqBP995hB6QkwipfYHigT8uhxi+7h0UU7+ZsoBBtwzgA3wM1vh6gemT+nODPrA3qigVP88qA5pEBxdWlOhY8Z72fJoyZ48WEp3q6geruTJCvSrM/YtE5X3z4LvQPAGWq05sVfO3aII+ISwoXvF8ONV5Zah0BD5SpjiTFKRXgr/7ueJB/eF8PsCdl8OmtcTg7pQKaq/sQ+E6odnbA6cl2Kz56i6VEGDE9LwQ8y9ENd598D0hPtrfgvFsHVAT9L/jqSSlUOaQv0NCIo/rmiwWpvEMG957+APv14I/HUG1vOwB/2oFWO1Rda/84s4FV43Ax4PcM2FqPgoeqq21fZDQguGFcxwU/pwXOrAbY14/fNvv1OL4FdU41UCY7joKHyvHWKB4DDzPCz2rh/bABPrvrAL5fDaywKqfKP3q5CbVOVXr4iVaoHG/5NbOBZUMiE/wHYRM8iv+W+vH7mw+A71UCK6TMX+UnWneXvTme0QBv2ZBM1/MsXy/8/OfTtyC+ffYjcN0KYAYU+an8RKr6QLY172Q2sKRPptuwTO9BA/+Y4LjkwEDQ+YC3twDZKslsgLvYn0h72syooT5o2Guh/br/7CfgTMmB7pMT1zbjf8PbmoFklWRuoVR6kOmoDKugPqA/1MR3yIRTTjh8hU0CpCuSzJuYu9i3juucDyqhfvpwE6hlCGsb2y58hRUZaMx8jKZyG7yXVKAXzvv6YePV5gFYIitfkYJvAtJY4+d4DFzI6ob1y6HO25fWBCHwXzZB+Yj4I1zDHHteu5XteFDn0cLGy838wY82/o47zUOJWdaPEY8Mzrk0b5kgDH6sEcpHG67ggk+1UVDHQ4lZ1i8pVzecm9KkhjK0ctuwTfvhd8rHGrhYNkJx37GegWiStLftTpVEwI+JoWxU7MayFS+koDLCyvgx37DpRuLs4C83bJMtl46Xm6Ksspjw5aNiKB9pUGC5CGWVxYIvG/nEg+WsiOJUjb/3ZsHhLzd8jVkuvUNMuBuQkmg+2Z0CVv42ZiEo3N1TYKgUZZUFaRsLQZU/TCirpLq64nk4bV7lvGHxqtLdTUNxH8XZuUPEJVU2KnZXWSRUrNCiXmvnosSsytG+lT28eAuNB1nfsHmRxVKCQieU26DoA6UHZHtLgmxvTpJtkiTJ2pQgWZt+QfM8GolTU+V/4TPriU6E/f/1F2g37z10XXwqAAAAAElFTkSuQmCC"
            )
        )

        false -> Toast.warning(
            message = msgError ?: "Unkown error",
            options = ToastOptions(
                avatar = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADAAAAAwCAYAAABXAvmHAAAACXBIWXMAAAsTAAALEwEAmpwYAAACgUlEQVR4nO1Zy2oUURBtNC5052vj4x+Enjq2EmioGoILl0ZFPyTqxo2PrENICP6BEhca/A9FP0DNRieGZO7tmLhpqRkddIL0496e7pE+UNDQiz6nbt17q04HQYsWLZyRzs8f3RO6YhkPLGPdCn2wQttG8ENDn63g/fAd7iddROnD4EhQN/bmOheNYNEIbVpBWiSM0GfDeJrEuDBx4v1rl84apjXDOChK/JAQxoFlrOwKnZ4IedOlO5bpmyvxQ8HYMtK5VRnxNAyPGcEz78RlvLSwmsbxjF/y18MTRvCmavJ2VFa0od/0mfnC5NPe9l9RSkTsYSXKlo2rADvcFyuO5Olu2TLwIkCQmi7dLEVejzUr6NUtwDK2+nF4pnj2mdZcNqI3AaJBy4XI6+3oekn5FGAY+wlH5/NnX7DoljHfKwA9lZ7kIq9NlvYpjRMgtKlNY6aAQVfp+LEqBFhBmnCITAGDlrihAmyX7uWp/5eNFSD0InsFdPBoqgCmdzlKyE+rXM0KoJddQh6GlKoEGMb+/y/ATnsJ2enfxFhvrADB8+m+yJgWMgWo6dRUAYl0KF8zx/jUNAGG8TG3m6eOmaea9RZG6HEu8r4GGq/kueBAo1BHoG7i9ncwloKi2JmLTlnG1yYM9btlfVPDl2/XLaDPuFGK/EiEYLU2AVyidMahc2iZIcfdWsRrbybvwNxl2qhsk8oYecGrNIqOeyE/EhHHMxM5mRhL3u31P6FepYvl+G/i9MV5wxY6YoWW9YJxLxf6rlnfmZ09GUwaejuqY1amd9LexggeJd2r54K6oU2Wmk7q26j1oYOHTnbajgx/4umUR29/vVvQrrIRv1lbtAimHz8BNz/RC6gTB7UAAAAASUVORK5CYII="
            ) // https://icons8.com/icons/set/warning
        )
    }
}
